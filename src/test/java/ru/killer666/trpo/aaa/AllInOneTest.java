package ru.killer666.trpo.aaa;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.h2.store.fs.FileUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.killer666.trpo.aaa.domains.*;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;
import ru.killer666.trpo.aaa.services.AccountingService;
import ru.killer666.trpo.aaa.services.AuthorizationService;
import ru.killer666.trpo.aaa.services.RoleResolverService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllInOneTest {
    private static final Logger logger = LogManager.getLogger(AllInOneTest.class);

    private static final String JDBC_URL = "jdbc:h2:./target/aaa";
    private static final String JDBC_USERNAME = "sa";
    private static final String JDBC_PASSWORD = "";

    private static Session session;
    private static AuthorizationService authorizationService;
    private static AccountingService accountingService;

    private static User authorizedUser;
    private static Accounting accounting;

    @Configuration
    @Import(BeanConfiguration.class)
    public static class TestBeanConfiguration {
        @Bean
        RoleResolverService roleResolverService() {
            return new RoleResolverServiceImpl();
        }
    }

    @ToString
    public enum RoleEnum {
        READ(1), WRITE(2), EXECUTE(4);

        @Getter
        private final int value;

        RoleEnum(int value) {
            this.value = value;
        }
    }

    public static class RoleResolverServiceImpl implements RoleResolverService {
        public Enum<?> resolve(int ordinal) {
            EnumSet<RoleEnum> set = EnumSet.allOf(RoleEnum.class);

            if (ordinal < set.size()) {
                Iterator iter = set.iterator();

                for (int i = 0; i < ordinal; i++) {
                    iter.next();
                }

                Enum<?> rval = (Enum<?>) iter.next();

                Preconditions.checkArgument(rval.ordinal() == ordinal);

                return rval;
            }

            throw new IllegalArgumentException("Invalid value " + ordinal + " for " + RoleEnum.class.getName() + ", must be < " + set.size());
        }
    }

    @BeforeClass
    public static void initialize() {
        logger.debug("Cleaning old data");
        FileUtils.tryDelete("target/aaa.mv.db");
        FileUtils.tryDelete("target/aaa.trace.db");

        logger.debug("Initialialing hibernate");

        System.setProperty("org.jboss.logging.provider", "slf4j");
        SessionFactory sessionFactory;

        try {
            Properties prop = new Properties();
            prop.setProperty("hibernate.hbm2ddl.auto", "update");
            prop.setProperty("hibernate.connection.url", JDBC_URL);
            prop.setProperty("hibernate.connection.username", JDBC_USERNAME);
            prop.setProperty("hibernate.connection.password", JDBC_PASSWORD);
            prop.setProperty("dialect", "org.hibernate.dialect.H2Dialect");
            prop.setProperty("org.jboss.logging.provider", "slf4j");

            AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration()
                    .addPackage("ru.killer666.trpo.aaa.domains")
                    .addProperties(prop);

            List<Class<?>> annotatedClasses = new ArrayList<>();

            for (Class cls : annotatedClasses) {
                annotationConfiguration.addAnnotatedClass(cls);
                logger.debug("Added custom annotated class: " + cls.getSimpleName());
            }

            annotationConfiguration.addAnnotatedClass(User.class)
                    .addAnnotatedClass(Resource.class)
                    .addAnnotatedClass(ResourceWithRole.class)
                    .addAnnotatedClass(Accounting.class)
                    .addAnnotatedClass(AccountingResource.class);

            sessionFactory = annotationConfiguration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }

        session = sessionFactory.openSession();

        logger.debug("Migrating");
        Flyway flyway = new Flyway();
        flyway.setDataSource(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        flyway.baseline();
        flyway.migrate();

        logger.debug("Initializing Spring app");
        ApplicationContext context = new AnnotationConfigApplicationContext(TestBeanConfiguration.class);

        authorizationService = context.getBean(AuthorizationService.class);
        accountingService = context.getBean(AccountingService.class);
    }

    @AfterClass
    public static void deinitialize() {
        session.close();
    }

    @Test
    public void test1Auth() {
        boolean result = false;

        try {
            authorizationService.authorizeUser(session, "XXX", "XXX");
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            result = true;
        }

        assertTrue("Auth result should be false", result);
    }

    @Test
    public void test2Auth() throws UserNotFoundException, IncorrectPasswordException {
        authorizedUser = authorizationService.authorizeUser(session, "jdoe", "sup3rpaZZ");
        accounting = accountingService.createForUser(authorizedUser);
    }

    @Test
    public void test3ResourceAccess() throws ResourceNotFoundException {
        boolean result = false;

        try {
            Resource resource = authorizationService.findResourceByName(session, "a.bc");
            authorizationService.authorizeOnResource(session, authorizedUser, resource, RoleEnum.WRITE);
        } catch (ResourceDeniedException e) {
            result = true;
        }

        assertTrue("Resource auth result should be false", result);
    }

    @Test
    public void test4ResourceAccess() throws ResourceNotFoundException, ResourceDeniedException {
        Resource resource = authorizationService.findResourceByName(session, "a.b");
        ResourceWithRole resourceWithRole = authorizationService.authorizeOnResource(session, authorizedUser, resource, RoleEnum.WRITE);

        accounting.pushResource(resourceWithRole);
    }

    @Test
    public void test5ResourceAccess() throws ResourceNotFoundException, ResourceDeniedException {
        Resource resource = authorizationService.findResourceByName(session, "a.bc");
        ResourceWithRole resourceWithRole = authorizationService.authorizeOnResource(session, authorizedUser, resource, RoleEnum.EXECUTE);

        accounting.pushResource(resourceWithRole);
    }

    @Test
    public void test6Accounting() throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        accounting.setLogoutDate(format.parse("2017-01-10"));
        accounting.increaseVolume(100);
        accountingService.save(session, accounting);
    }
}

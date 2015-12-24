package ru.killer666.trpo.aaa;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.h2.store.fs.FileUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.killer666.trpo.aaa.domains.Accounting;
import ru.killer666.trpo.aaa.domains.Resource;
import ru.killer666.trpo.aaa.domains.ResourceWithRole;
import ru.killer666.trpo.aaa.domains.User;
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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AllInOneTest {
    private static final Logger logger = LogManager.getLogger(AllInOneTest.class);

    private static final String JDBC_URL = "jdbc:h2:./target/aaa";
    private static final String JDBC_USERNAME = "sa";
    private static final String JDBC_PASSWORD = "";

    private static AuthorizationService authorizationService;
    private static AccountingService accountingService;

    private static User authorizedUser;
    private static Accounting accounting;

    @Configuration
    @EnableTransactionManagement
    @Import(BeanConfiguration.class)
    public static class TestBeanConfiguration {
        @Bean
        RoleResolverService roleResolverService() {
            return new RoleResolverServiceImpl();
        }

        @Bean
        public HibernateTransactionManager hibernateTransactionManager() {
            HibernateTransactionManager hibernateTransactionManager = new HibernateTransactionManager();

            hibernateTransactionManager.setDataSource(this.driverManagerDataSource());
            hibernateTransactionManager.setSessionFactory(this.localSessionFactoryBean().getObject());

            return hibernateTransactionManager;
        }

        @Bean
        public DriverManagerDataSource driverManagerDataSource() {
            DriverManagerDataSource driverManagerDataSource = new DriverManagerDataSource();

            driverManagerDataSource.setUrl(JDBC_URL);
            driverManagerDataSource.setUsername(JDBC_USERNAME);
            driverManagerDataSource.setPassword(JDBC_PASSWORD);

            return driverManagerDataSource;
        }

        @Bean
        public LocalSessionFactoryBean localSessionFactoryBean() {
            System.setProperty("org.jboss.logging.provider", "slf4j");

            LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();

            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
            hibernateProperties.setProperty("hibernate.current_session_context_class", "org.hibernate.context.internal.ThreadLocalSessionContext");
            hibernateProperties.setProperty("dialect", "org.hibernate.dialect.H2Dialect");

            localSessionFactoryBean.setHibernateProperties(hibernateProperties);
            localSessionFactoryBean.setPackagesToScan("ru.killer666.trpo.aaa");
            localSessionFactoryBean.setDataSource(this.driverManagerDataSource());

            return localSessionFactoryBean;
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
        logger.warn("Cleaning old data");
        FileUtils.tryDelete("target/aaa.mv.db");
        FileUtils.tryDelete("target/aaa.trace.db");

        System.setProperty("org.jboss.logging.provider", "slf4j");

        logger.warn("Initializing Spring app");
        ApplicationContext context = new AnnotationConfigApplicationContext(TestBeanConfiguration.class);

        authorizationService = context.getBean(AuthorizationService.class);
        accountingService = context.getBean(AccountingService.class);

        logger.warn("Migrating");
        Flyway flyway = new Flyway();
        flyway.setDataSource(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        flyway.baseline();
        flyway.migrate();
    }

    @Test
    public void test1Auth() {
        boolean result = false;

        try {
            authorizationService.authorizeUser("XXX", "XXX");
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            result = true;
        }

        assertTrue("Auth result should be false", result);
    }

    @Test
    public void test2Auth() throws UserNotFoundException, IncorrectPasswordException {
        authorizedUser = authorizationService.authorizeUser("jdoe", "sup3rpaZZ");
        accounting = accountingService.createForUser(authorizedUser);
    }

    @Test
    public void test3ResourceAccess() throws ResourceNotFoundException {
        boolean result = false;

        try {
            Resource resource = authorizationService.findResourceByName("a.bc");
            authorizationService.authorizeOnResource(authorizedUser, resource, RoleEnum.WRITE);
        } catch (ResourceDeniedException e) {
            result = true;
        }

        assertTrue("Resource auth result should be false", result);
    }

    @Test
    public void test4ResourceAccess() throws ResourceNotFoundException, ResourceDeniedException {
        Resource resource = authorizationService.findResourceByName("a.b");
        ResourceWithRole resourceWithRole = authorizationService.authorizeOnResource(authorizedUser, resource, RoleEnum.WRITE);

        accounting.pushResource(resourceWithRole);
    }

    @Test
    public void test5ResourceAccess() throws ResourceNotFoundException, ResourceDeniedException {
        Resource resource = authorizationService.findResourceByName("a.bc");
        ResourceWithRole resourceWithRole = authorizationService.authorizeOnResource(authorizedUser, resource, RoleEnum.EXECUTE);

        accounting.pushResource(resourceWithRole);
    }

    @Test
    public void test6Accounting() throws ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        accounting.setLogoutDate(format.parse("2017-01-10"));
        accounting.increaseVolume(100);
        accountingService.save(accounting);
    }
}

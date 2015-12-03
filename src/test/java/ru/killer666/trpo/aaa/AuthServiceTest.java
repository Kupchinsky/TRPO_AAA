package ru.killer666.trpo.aaa;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;
import ru.killer666.trpo.aaa.services.AuthService;
import ru.killer666.trpo.aaa.services.HibernateService;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthServiceTest {

    private static final Logger logger = LogManager.getLogger(AuthServiceTest.class);
    private static final String JDBC_URL = "jdbc:h2:./target/aaa";
    private static final String JDBC_USERNAME = "sa";
    private static final String JDBC_PASSWORD = "";

    private static AuthService authService = new AuthService(new HibernateService(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD, "org.hibernate.dialect.H2Dialect"), Role.class);
    private static Session session;

    @BeforeClass
    public static void initialize() {
        logger.debug("Migrating");

        Flyway flyway = new Flyway();
        flyway.setDataSource(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        flyway.baseline();
        flyway.migrate();

        session = authService.getHibernateService().getSession();
    }

    @AfterClass
    public static void deinitialize() {
        session.close();
    }

    @Test
    public void test1Auth() throws SQLException {
        boolean result = false;

        try {
            authService.authUser(session, "XXX", "XXX");
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            result = true;
        }

        assertTrue("Auth result should be false", result);
    }

    @Test
    public void test2Auth() throws SQLException, UserNotFoundException, IncorrectPasswordException {
        authService.authUser(session, "jdoe", "sup3rpaZZ");
    }

    @Test
    public void test3ResourceAccess() throws SQLException, ResourceNotFoundException {

        boolean result = false;

        try {
            authService.authResource(session, authService.getResourceByName(session, "a.bc"), Role.WRITE);
        } catch (ResourceDeniedException e) {
            result = true;
        }

        assertTrue("Resource auth result should be false", result);
    }

    @Test
    public void test4ResourceAccess() throws SQLException, ResourceNotFoundException, ResourceDeniedException {
        authService.authResource(session, authService.getResourceByName(session, "a.b"), Role.WRITE);
    }

    @Test
    public void test5ResourceAccess() throws SQLException, ResourceNotFoundException, ResourceDeniedException {
        authService.authResource(session, authService.getResourceByName(session, "a.bc"), Role.EXECUTE);
    }

    @Test
    public void test6Accounting() throws SQLException, ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        authService.getLogOnUserAccounting().setLogoutDate(format.parse("2017-01-10"));
        authService.getLogOnUserAccounting().increaseVolume(100);
        authService.saveAccounting(session);
    }

    @ToString
    public enum Role implements RoleInterface {
        READ(1), WRITE(2), EXECUTE(4);

        @Getter
        private final int value;

        Role(int value) {
            this.value = value;
        }
    }
}

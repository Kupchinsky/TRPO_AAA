package ru.killer666.trpo.aaa;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;
import ru.killer666.trpo.aaa.services.AuthService;
import ru.killer666.trpo.aaa.services.HibernateService;

import java.sql.SQLException;

import static org.junit.Assert.assertTrue;

public class AuthServiceTest {

    private static final Logger logger = LogManager.getLogger(AuthServiceTest.class);
    private static final String JDBC_URL = "jdbc:mysql://localhost/trpo_aaa";
    private static final String JDBC_USERNAME = "root";
    private static final String JDBC_PASSWORD = "5`7478";
//"org.hibernate.dialect.H2Dialect"
    private static AuthService authService = new AuthService(new HibernateService(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD, "org.hibernate.dialect.MySQL5Dialect"), Role.class);

    @BeforeClass
    public static void initialize() {
        logger.debug("Migrating");

        Flyway flyway = new Flyway();
        flyway.setDataSource(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        flyway.baseline();
        flyway.migrate();
    }

    @Test
    public void testAuth1() throws SQLException {
        boolean result = false;

        try {
            authService.authUser("XXX", "XXX");
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            result = true;
        }

        assertTrue("Auth result should be false", result);
    }

    @Test
    public void testAuth2() throws SQLException, UserNotFoundException, IncorrectPasswordException {
        authService.authUser("jdoe", "sup3rpaZZ");
    }

    @Test
    public void testResourceAccess1() throws SQLException, ResourceNotFoundException {

        boolean result = false;

        try {
            authService.authResource(authService.getResourceByName("a.bc"), Role.WRITE);
        } catch (ResourceDeniedException e) {
            result = true;
        }

        assertTrue("Resource auth result should be false", result);
    }
/*
    @Test
    public void testResourceAccess2() throws SQLException, ResourceNotFoundException, ResourceDeniedException {
        authService.authResource(authService.getResourceByName("a.b"), Role.READ);
    }

    @Test
    public void testResourceAccess3() throws SQLException, ResourceNotFoundException, ResourceDeniedException {
        authService.authResource(authService.getResourceByName("a.b"), Role.EXECUTE);
    }

    @Test
    public void testAccounting() throws SQLException, ParseException {

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        authService.getLogOnUserAccounting().setLogoutDate(format.parse("2017-01-10"));
        authService.getLogOnUserAccounting().increaseVolume(100);
        authService.saveAccounting();
    }
*/
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

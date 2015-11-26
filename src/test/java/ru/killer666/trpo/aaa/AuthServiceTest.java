package ru.killer666.trpo.aaa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.killer666.trpo.aaa.views.ConsoleMain;

import static org.junit.Assert.assertEquals;

public class AuthServiceTest {

    private static final Logger logger = LogManager.getLogger(ConsoleMainTest.class);
    private static final ConsoleMain consoleMain = new ConsoleMain();

    @BeforeClass
    public static void fillData() {
        ConsoleMainTest.logger.debug("Filling data");
        ConsoleMainTest.consoleMain.createAuthService();

        Flyway flyway = new Flyway();
        flyway.setDataSource(ConsoleMain.DB_URL, ConsoleMain.DB_USERNAME, ConsoleMain.DB_PASSWORD);
        flyway.baseline();
        flyway.migrate();
    }

    /* C1 */
    @Test
    public void testC11() {
        ConsoleMain.ResultCode result = ConsoleMainTest.consoleMain.work(new String[]{});
        assertEquals(ConsoleMain.ResultCode.INVALIDINPUT, result);
    }

    @Test
    public void testC12() {
        ConsoleMain.ResultCode result = ConsoleMainTest.consoleMain.work(new String[]{"-h"});
        assertEquals(ConsoleMain.ResultCode.INVALIDINPUT, result);
    }

    /* C2 */
    @Test
    public void testC21() {
        ConsoleMain.ResultCode result = ConsoleMainTest.consoleMain.work(new String[]{"-login", "XXX", "-pass", "XXX"});
        assertEquals(ConsoleMain.ResultCode.USERNOTFOUND, result);
    }

    @Test
    public void testC22() {
        ConsoleMain.ResultCode result = ConsoleMainTest.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "XXX"});
        assertEquals(ConsoleMain.ResultCode.INCORRECTPASSWORD, result);
    }

    @Test
    public void testC23() {
        ConsoleMain.ResultCode result = ConsoleMainTest.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ"});
        assertEquals(ConsoleMain.ResultCode.SUCCESS, result);
    }

}

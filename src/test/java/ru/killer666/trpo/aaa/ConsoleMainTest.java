package ru.killer666.trpo.aaa;

import org.junit.Test;
import ru.killer666.trpo.aaa.views.ConsoleMain;

import static org.junit.Assert.assertEquals;

public class ConsoleMainTest {

    private ConsoleMain consoleMain = new ConsoleMain();

    /* C1 */
    @Test
    public void testC11() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{});
        assertEquals(ConsoleMain.ResultCode.INVALIDINPUT, result);
    }

    @Test
    public void testC12() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-h"});
        assertEquals(ConsoleMain.ResultCode.INVALIDINPUT, result);
    }

    /* C2 */
    @Test
    public void testC21() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "XXX", "-pass", "XXX"});
        assertEquals(ConsoleMain.ResultCode.USERNOTFOUND, result);
    }

    @Test
    public void testC22() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "XXX"});
        assertEquals(ConsoleMain.ResultCode.INCORRECTPASSWORD, result);
    }

    @Test
    public void testC23() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ"});
        assertEquals(ConsoleMain.ResultCode.SUCCESS, result);
    }

    /* C3 */
    @Test
    public void testC31() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a"});
        assertEquals(ConsoleMain.ResultCode.SUCCESS, result);
    }

    @Test
    public void testC32() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b"});
        assertEquals(ConsoleMain.ResultCode.SUCCESS, result);
    }

    @Test
    public void testC33() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "XXX", "-res", "a.b"});
        assertEquals(ConsoleMain.ResultCode.INVALIDROLE, result);
    }

    @Test
    public void testC34() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "XXX"});
        assertEquals(ConsoleMain.ResultCode.RESOURCEDENIED, result);
    }

    @Test
    public void testC35() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "WRITE", "-res", "a"});
        assertEquals(ConsoleMain.ResultCode.RESOURCEDENIED, result);
    }

    @Test
    public void testC36() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "WRITE", "-res", "a.bc"});
        assertEquals(ConsoleMain.ResultCode.RESOURCEDENIED, result);
    }

    /* C4 */
    @Test
    public void testC41() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "2015-01-01", "-de", "2015-12-31", "-vol", "100"});
        assertEquals(ConsoleMain.ResultCode.SUCCESS, result);
    }

    @Test
    public void testC42() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "01-01-2015", "-de", "2015-12-31", "-vol", "100"});
        assertEquals(ConsoleMain.ResultCode.INCORRECTACTIVITY, result);
    }

    @Test
    public void testC43() {
        ConsoleMain.ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "2015-01-01", "-de", "2015-12-31", "-vol", "XXX"});
        assertEquals(ConsoleMain.ResultCode.INCORRECTACTIVITY, result);
    }
}

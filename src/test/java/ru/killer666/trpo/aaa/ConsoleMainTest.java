package ru.killer666.trpo.aaa;

import junit.framework.TestCase;
import org.junit.Test;

public class ConsoleMainTest extends TestCase {

    private ConsoleMain consoleMain = new ConsoleMain();

    /* C1 */
    @Test
    public void testC11() {
        ResultCode result = this.consoleMain.work(new String[]{});
        assertEquals(ResultCode.INVALIDINPUT, result);
    }

    @Test
    public void testC12() {
        ResultCode result = this.consoleMain.work(new String[]{"-h"});
        assertEquals(ResultCode.INVALIDINPUT, result);
    }

    /* C2 */
    @Test
    public void testC21() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "XXX", "-pass", "XXX"});
        assertEquals(ResultCode.USERNOTFOUND, result);
    }

    @Test
    public void testC22() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "XXX"});
        assertEquals(ResultCode.INCORRECTPASSWORD, result);
    }

    @Test
    public void testC23() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ"});
        assertEquals(ResultCode.SUCCESS, result);
    }

    /* C3 */
    @Test
    public void testC31() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a"});
        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    public void testC32() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b"});
        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    public void testC33() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "XXX", "-res", "a.b"});
        assertEquals(ResultCode.INVALIDROLE, result);
    }

    @Test
    public void testC34() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "XXX"});
        assertEquals(ResultCode.RESOURCEDENIED, result);
    }

    @Test
    public void testC35() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "WRITE", "-res", "a"});
        assertEquals(ResultCode.RESOURCEDENIED, result);
    }

    @Test
    public void testC36() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "WRITE", "-res", "a.bc"});
        assertEquals(ResultCode.RESOURCEDENIED, result);
    }

    /* C4 */
    @Test
    public void testC41() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "2015-01-01", "-de", "2015-12-31", "-vol", "100"});
        assertEquals(ResultCode.SUCCESS, result);
    }

    @Test
    public void testC42() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "01-01-2015", "-de", "2015-12-31", "-vol", "100"});
        assertEquals(ResultCode.INCORRECTACTIVITY, result);
    }

    @Test
    public void testC43() {
        ResultCode result = this.consoleMain.work(new String[]{"-login", "jdoe", "-pass", "sup3rpaZZ", "-role", "READ", "-res", "a.b", "-ds", "2015-01-01", "-de", "2015-12-31", "-vol", "100"});
        assertEquals(ResultCode.INCORRECTACTIVITY, result);
    }
}

package ru.killer666.trpo.aaa;

import lombok.Getter;
import lombok.Setter;
import ru.killer666.trpo.aaa.Models.Accounting;
import ru.killer666.trpo.aaa.Models.User;

public class UserController {
    @Getter
    private User logOnUser = null;
    @Getter
    @Setter
    private Accounting userAccounting = null;
    private static Database db = new Database(DatabaseConfig.host, DatabaseConfig.port, DatabaseConfig.database, DatabaseConfig.userName, DatabaseConfig.password);

    public void logIn(String userName, String password, String resourceName) {
        // TODO: Find user in database
        // TODO: Check password
        // TODO: Check resource access
    }

    public void logOut() {
        // TODO: Write "userAccounting" into database
    }

    public static class UserNotFoundException extends Exception {
    }

    public static class IncorrectPasswordException extends Exception {
    }

    public static class ResourceDeniedException extends Exception {
    }
}

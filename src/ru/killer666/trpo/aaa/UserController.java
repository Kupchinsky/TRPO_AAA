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

    public boolean logIn(String userName, String password) {
        return false;
    }

    public boolean logOut() {
        return false;
    }
}

package ru.killer666.trpo.aaa;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import ru.killer666.trpo.aaa.Models.Accounting;
import ru.killer666.trpo.aaa.Models.Resource;
import ru.killer666.trpo.aaa.Models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserController {
    @Getter
    private User logOnUser = null;
    @Getter
    private Resource resource = null;
    @Getter
    @Setter
    private Accounting userAccounting = null;
    private static Database db = new Database(DatabaseConfig.host, DatabaseConfig.port, DatabaseConfig.database, DatabaseConfig.userName, DatabaseConfig.password);

    private boolean isValidPassword(String password, String passwordHash, String salt) {
        return DigestUtils.sha1Hex(password + salt).equals(passwordHash);
    }

    public void logIn(String userName, String password, String resourceName) throws UserNotFoundException, IncorrectPasswordException, ResourceDeniedException {
        PreparedStatement preparedStatement = null;

        try {
            preparedStatement = UserController.db.getConnection().prepareStatement("SELECT * FROM `users` WHERE `login`=? LIMIT 1");
            preparedStatement.setString(1, userName);
            ResultSet result = preparedStatement.executeQuery();

            if (!result.next())
                throw new UserNotFoundException();

            String passwordHash = result.getString("passwordHash");
            String salt = result.getString("salt");

            if (!this.isValidPassword(password, passwordHash, salt))
                throw new IncorrectPasswordException();

            // TODO: Check resource access
        } catch (SQLException e) {
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                    preparedStatement.getConnection().close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
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

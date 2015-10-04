package ru.killer666.trpo.aaa;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import ru.killer666.trpo.aaa.models.Accounting;
import ru.killer666.trpo.aaa.models.Resource;
import ru.killer666.trpo.aaa.models.Role;
import ru.killer666.trpo.aaa.models.User;

import java.sql.*;

public class UserController {
    @Getter
    private User logOnUser = null;
    @Getter
    private Accounting logOnUserAccounting = null;

    private static Database db = new Database(DatabaseConfig.host, DatabaseConfig.port, DatabaseConfig.database, DatabaseConfig.userName, DatabaseConfig.password);

    private String encryptPassword(String password, String salt) {
        return DigestUtils.sha1Hex(DigestUtils.sha1Hex(password) + salt);
    }

    public void logIn(String userName, String password, String resourceName, int role) throws UserNotFoundException, IncorrectPasswordException, ResourceDeniedException, ResourceNotFoundException {
        Connection connection = null;

        try {
            connection = UserController.db.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE `login`=?");
            preparedStatement.setString(1, userName);
            ResultSet resultUser = preparedStatement.executeQuery();

            if (!resultUser.first()) {
                throw new UserNotFoundException();
            }

            // Checking password
            if (this.encryptPassword(password, resultUser.getString("salt")).equals(resultUser.getString("passwordHash"))) {
                throw new IncorrectPasswordException();
            }

            // Checking resource exists
            preparedStatement = UserController.db.getConnection().prepareStatement("SELECT * FROM `resources` WHERE `name`=?");
            preparedStatement.setString(1, resourceName);
            ResultSet resultResource = preparedStatement.executeQuery();

            if (!resultResource.first()) {
                throw new ResourceNotFoundException();
            }

            // Checking resource access
            boolean accessGranted = false;
            int lastResourceId = resultResource.getInt("id");
            int lastParentResourceId = resultResource.getInt("parent_resource_id");
            boolean hasParentResourceId = !resultResource.wasNull();

            while (true) {
                // Checking access for this resource
                preparedStatement = UserController.db.getConnection().prepareStatement("SELECT * FROM `resources_users` WHERE `resource_id`=? AND `user_id`=?");
                preparedStatement.setInt(1, lastResourceId);
                preparedStatement.setInt(2, resultUser.getInt("id"));
                ResultSet resultAccess = preparedStatement.executeQuery();

                if (resultAccess.first()) {
                    accessGranted = true;
                    break;
                }

                // Finding parent resources with access
                if (!hasParentResourceId) {
                    break;
                }

                preparedStatement = UserController.db.getConnection().prepareStatement("SELECT * FROM `resources` WHERE `id`=?");
                preparedStatement.setInt(1, lastParentResourceId);

                ResultSet resultParentResource = preparedStatement.executeQuery();
                lastResourceId = resultParentResource.getInt("id");
                lastParentResourceId = resultParentResource.getInt("parent_resource_id");
                hasParentResourceId = !resultParentResource.wasNull();
            }

            if (!accessGranted) {
                throw new ResourceDeniedException();
            }

            this.logOnUser = new User(resultUser.getInt("id"), resultUser.getString("login"), resultUser.getString("passwordHash"), resultUser.getString("salt"), resultUser.getString("personName"));
            this.logOnUserAccounting = new Accounting(this.logOnUser, Role.fromInt(role));

            // Finding all child resources
            Resource lastParentResource = new Resource(resultResource.getInt("id"), resultResource.getString("name"), null);
            this.logOnUserAccounting.getResources().add(lastParentResource);

            while (true) {
                preparedStatement = UserController.db.getConnection().prepareStatement("SELECT * FROM `resources` WHERE `parent_resource_id`=?");
                preparedStatement.setInt(1, lastParentResource.getDatabaseId());

                ResultSet resultChildResource = preparedStatement.executeQuery();

                if (!resultChildResource.first()) {
                    break;
                }

                Resource newResource = new Resource(resultChildResource.getInt("id"), resultChildResource.getString("name"), lastParentResource);
                this.logOnUserAccounting.getResources().add(newResource);

                lastParentResource = newResource;
            }
        } catch (SQLException e) {
            // Error while working
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void logOut() {
        if (this.logOnUser == null)
            return;

        Connection connection = null;

        try {
            connection = UserController.db.getConnection();
            connection.setAutoCommit(false);

            // Write user accounting into database
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `accounting` (`user_id`, `role`, `volume`, `logon_date`, `logout_date`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, this.logOnUser.getDatabaseId());
            preparedStatement.setInt(2, this.logOnUserAccounting.getRole().getValue());
            preparedStatement.setInt(3, this.logOnUserAccounting.getVolume());
            preparedStatement.setTimestamp(4, new java.sql.Timestamp(this.logOnUserAccounting.getLoginDate().getTime()));
            preparedStatement.setTimestamp(5, new java.sql.Timestamp(this.logOnUserAccounting.getLoginDate().getTime()));

            if (preparedStatement.executeUpdate() == 0)
                throw new SQLException("Creating accounting item failed, no rows affected.");

            int accountingId;

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    accountingId = (int) generatedKeys.getLong(1);
                else
                    throw new SQLException("Creating accounting item failed, no ID obtained.");
            }

            // Write all resources
            for (Resource resource : this.logOnUserAccounting.getResources()) {
                preparedStatement = connection.prepareStatement("INSERT INTO `accounting_resources` (`accounting_id`, `resource_id`) VALUES (?, ?)");
                preparedStatement.setInt(1, accountingId);
                preparedStatement.setInt(2, resource.getDatabaseId());

                if (preparedStatement.executeUpdate() == 0)
                    throw new SQLException("Creating accounting resource item failed, no rows affected.");
            }

            connection.commit();

            this.logOnUserAccounting = null;
            this.logOnUser = null;
        } catch (SQLException e) {
            // Error while working
            e.printStackTrace();

            try {
                if (connection != null)
                    connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class UserNotFoundException extends Exception {
    }

    public static class IncorrectPasswordException extends Exception {
    }

    public static class ResourceDeniedException extends Exception {
    }

    public static class ResourceNotFoundException extends Exception {
    }
}

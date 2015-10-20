package ru.killer666.trpo.aaa;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.killer666.trpo.aaa.domains.Accounting;
import ru.killer666.trpo.aaa.domains.Resource;
import ru.killer666.trpo.aaa.domains.Role;
import ru.killer666.trpo.aaa.domains.User;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;

import java.sql.*;
import java.util.Calendar;

public class UserController implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Getter
    private User logOnUser = null;
    @Getter
    private Accounting logOnUserAccounting = null;
    private final Database db = new Database(DatabaseConfig.userName, DatabaseConfig.password);

    private String encryptPassword(String password, String salt) {
        return DigestUtils.shaHex(DigestUtils.shaHex(password) + salt);
    }

    public void authResource(String resourceName) throws ResourceNotFoundException, ResourceDeniedException, SQLException {
        UserController.logger.debug("Authorizing resource");

        try (Connection connection = this.db.getConnection()) {
            // Checking resource exists
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `resources` WHERE `name`=?");
            preparedStatement.setString(1, resourceName);
            ResultSet resultResource = preparedStatement.executeQuery();

            if (!resultResource.first()) {
                throw new ResourceNotFoundException(resourceName);
            }

            // Checking resource access
            boolean accessGranted = false;
            int lastResourceId = resultResource.getInt("id");
            int lastParentResourceId = resultResource.getInt("parent_resource_id");
            boolean hasParentResourceId = !resultResource.wasNull();

            while (true) {
                // Checking access for this resource
                preparedStatement = connection.prepareStatement("SELECT * FROM `resources_users` WHERE `resource_id`=? AND `user_id`=? AND `role`=?");
                preparedStatement.setInt(1, lastResourceId);
                preparedStatement.setInt(2, this.logOnUser.getDatabaseId());
                preparedStatement.setInt(3, this.logOnUserAccounting.getRole().getValue());
                ResultSet resultAccess = preparedStatement.executeQuery();

                if (resultAccess.first()) {
                    accessGranted = true;
                    break;
                }

                // Finding parent resources with access
                if (!hasParentResourceId) {
                    break;
                }

                preparedStatement = connection.prepareStatement("SELECT * FROM `resources` WHERE `id`=?");
                preparedStatement.setInt(1, lastParentResourceId);

                ResultSet resultParentResource = preparedStatement.executeQuery();

                if (!resultParentResource.first()) {
                    break;
                }

                lastResourceId = resultParentResource.getInt("id");
                lastParentResourceId = resultParentResource.getInt("parent_resource_id");
                hasParentResourceId = !resultParentResource.wasNull();
            }

            if (!accessGranted) {
                throw new ResourceDeniedException(resourceName, this.logOnUser.getLogin());
            }

            // Finding all child resources
            Resource lastParentResource = new Resource(resultResource.getInt("id"), resultResource.getString("name"), null);
            this.logOnUserAccounting.getResources().add(lastParentResource);

            while (true) {
                preparedStatement = this.db.getConnection().prepareStatement("SELECT * FROM `resources` WHERE `parent_resource_id`=?");
                preparedStatement.setInt(1, lastParentResource.getDatabaseId());

                ResultSet resultChildResource = preparedStatement.executeQuery();

                if (!resultChildResource.first()) {
                    break;
                }

                Resource newResource = new Resource(resultChildResource.getInt("id"), resultChildResource.getString("name"), lastParentResource);
                this.logOnUserAccounting.getResources().add(newResource);

                lastParentResource = newResource;
            }

            UserController.logger.debug("Authorized on resource " + resourceName);
        } catch (SQLException e) {
            UserController.logger.error("Authorizing resource failed!", e);
            throw e;
        }
    }

    public void authUser(String userName, String password) throws UserNotFoundException, IncorrectPasswordException, SQLException {
        UserController.logger.debug("Authorizing user");

        try (Connection connection = this.db.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE `login`=?");
            preparedStatement.setString(1, userName);
            ResultSet resultUser = preparedStatement.executeQuery();

            if (!resultUser.first()) {
                throw new UserNotFoundException(userName);
            }

            // Checking password
            if (!this.encryptPassword(password, resultUser.getString("salt")).equals(resultUser.getString("passwordHash"))) {
                throw new IncorrectPasswordException(userName, password);
            }

            this.logOnUser = new User(resultUser.getInt("id"), resultUser.getString("login"), resultUser.getString("passwordHash"), resultUser.getString("salt"), resultUser.getString("personName"));
            UserController.logger.debug("Authorized as user " + this.logOnUser.getLogin() + " (" + this.logOnUser.getPersonName() + ")");
        } catch (SQLException e) {
            UserController.logger.error("Authorizing user failed!", e);
            throw e;
        }
    }

    public void createAccounting(Role role) {
        this.logOnUserAccounting = new Accounting(this.logOnUser, role);
    }

    public void saveAccounting() throws SQLException {
        if (this.logOnUserAccounting.getLogoutDate() == null) {
            this.logOnUserAccounting.setLogoutDate(Calendar.getInstance().getTime());
            UserController.logger.debug("Logout date == null, setting up to current time");
        }

        UserController.logger.debug("Saving accounting");

        try (Connection connection = this.db.getConnection()) {
            connection.setAutoCommit(false);

            // Write user accounting into database
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `accounting` (`user_id`, `role`, `volume`, `logon_date`, `logout_date`) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, this.logOnUser.getDatabaseId());
            preparedStatement.setInt(2, this.logOnUserAccounting.getRole().getValue());
            preparedStatement.setInt(3, this.logOnUserAccounting.getVolume());
            preparedStatement.setTimestamp(4, new java.sql.Timestamp(this.logOnUserAccounting.getLoginDate().getTime()));
            preparedStatement.setTimestamp(5, new java.sql.Timestamp(this.logOnUserAccounting.getLogoutDate().getTime()));

            if (preparedStatement.executeUpdate() == 0)
                throw new SQLException("Creating accounting item failed, no rows affected.");

            int accountingId;

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    accountingId = (int) generatedKeys.getLong(1);
                else
                    throw new SQLException("Creating accounting item failed, no ID obtained.");
            }

            // Write resource
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
            UserController.logger.error("Save accounting failed!", e);
            throw e;
        }
    }

    public void clearAll() {
        this.logOnUserAccounting = null;
        this.logOnUser = null;
    }

    @Override
    public void close() {
        UserController.logger.debug("Closing resources");
        this.db.closePool();
    }
}

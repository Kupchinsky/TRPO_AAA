package ru.killer666.trpo.aaa;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.killer666.trpo.aaa.domains.Accounting;
import ru.killer666.trpo.aaa.domains.Resource;
import ru.killer666.trpo.aaa.domains.RoleInterface;
import ru.killer666.trpo.aaa.domains.User;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;

import java.sql.*;
import java.util.*;

public class UserController implements AutoCloseable {

    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Getter
    private User logOnUser = null;
    @Getter
    private Accounting logOnUserAccounting = null;
    @Getter
    private final Database db = new Database(DatabaseConfig.userName, DatabaseConfig.password);

    private String encryptPassword(String password, String salt) {
        return DigestUtils.shaHex(DigestUtils.shaHex(password) + salt);
    }

    public List<Resource> getAllResources() throws SQLException {
        UserController.logger.debug("Fetching all resources");

        try (Connection connection = this.db.getConnection()) {

            ResultSet resultResources = connection.createStatement().executeQuery("SELECT * FROM `resources`");
            List<Resource> result = new ArrayList<>();
            Map<Resource, Integer> parentIds = new HashMap<>();

            while (resultResources.next()) {

                Resource resultResource = new Resource(resultResources.getInt("id"), resultResources.getString("name"), null);
                int parentResourceId = resultResources.getInt("parent_resource_id");

                if (!resultResources.wasNull())
                    parentIds.put(resultResource, parentResourceId);

                result.add(resultResource);
            }

            for (Map.Entry<Resource, Integer> entry : parentIds.entrySet()) {

                for (Resource resource : result) {
                    if (resource.getDatabaseId() == entry.getValue()) {
                        entry.getKey().setParentResource(resource);
                        break;
                    }
                }

            }

            return result;
        } catch (SQLException e) {
            UserController.logger.error("Fetching resources failed!", e);
            throw e;
        }
    }

    /* Для внешнего использования */
    @SuppressWarnings("unused")
    public List<Integer> getGrantedRoles(Resource resource) throws SQLException {
        UserController.logger.debug("Getting roles for resource");

        try (Connection connection = this.db.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT `role` FROM `resources_users` WHERE `resource_id`=? AND `user_id`=?");
            preparedStatement.setInt(1, resource.getDatabaseId());
            preparedStatement.setInt(2, this.logOnUser.getDatabaseId());

            ResultSet resultSet = preparedStatement.executeQuery();

            List<Integer> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(resultSet.getInt("role"));
            }

            return result;
        } catch (SQLException e) {
            UserController.logger.error("Fetching roles for resource failed!", e);
            throw e;
        }
    }

    public void authResource(Resource resource, RoleInterface role) throws ResourceNotFoundException, ResourceDeniedException, SQLException {
        UserController.logger.debug("Authorizing resource");

        try (Connection connection = this.db.getConnection()) {
            // Checking access for this resource
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `resources_users` WHERE `resource_id`=? AND `user_id`=? AND `role`=?");
            preparedStatement.setInt(1, resource.getDatabaseId());
            preparedStatement.setInt(2, this.logOnUser.getDatabaseId());
            preparedStatement.setInt(3, role.getValue());

            ResultSet resultAccess = preparedStatement.executeQuery();

            if (!resultAccess.first()) {
                throw new ResourceDeniedException(resource.getName(), this.logOnUser.getLogin());
            }

            this.logOnUserAccounting.getResources().put(resource, role);

            UserController.logger.debug("Authorized on resource " + resource.getName());
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
            this.logOnUserAccounting = new Accounting(this.logOnUser);

            UserController.logger.debug("Authorized as user " + this.logOnUser.getLogin() + " (" + this.logOnUser.getPersonName() + ")");
        } catch (SQLException e) {
            UserController.logger.error("Authorizing user failed!", e);
            throw e;
        }
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
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `accounting` (`user_id`, `volume`, `logon_date`, `logout_date`) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, this.logOnUser.getDatabaseId());
            preparedStatement.setInt(2, this.logOnUserAccounting.getVolume());
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(this.logOnUserAccounting.getLoginDate().getTime()));
            preparedStatement.setTimestamp(4, new java.sql.Timestamp(this.logOnUserAccounting.getLogoutDate().getTime()));

            if (preparedStatement.executeUpdate() == 0)
                throw new SQLException("Creating accounting item failed, no rows affected.");

            int accountingId;

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next())
                    accountingId = (int) generatedKeys.getLong(1);
                else
                    throw new SQLException("Creating accounting item failed, no ID obtained.");
            }

            // Write resources
            for (Map.Entry<Resource, RoleInterface> entry : this.logOnUserAccounting.getResources().entries()) {
                preparedStatement = connection.prepareStatement("INSERT INTO `accounting_resources` (`accounting_id`, `resource_id`, `role`) VALUES (?, ?, ?)");
                preparedStatement.setInt(1, accountingId);
                preparedStatement.setInt(2, entry.getKey().getDatabaseId());
                preparedStatement.setInt(3, entry.getValue().getValue());

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

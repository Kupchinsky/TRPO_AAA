package ru.killer666.trpo.aaa.services;

import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ru.killer666.trpo.aaa.RoleInterface;
import ru.killer666.trpo.aaa.domains.*;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AuthService {

    private static final Logger logger = LogManager.getLogger(AuthService.class);

    @Getter
    private User logOnUser = null;
    @Getter
    private Accounting logOnUserAccounting = null;
    @Getter
    private final HibernateService hibernateService;
    @Getter
    private final Class<? extends RoleInterface> roleInterfaceClass;

    public AuthService(HibernateService hibernateService, Class<? extends RoleInterface> roleInterfaceClass) {
        this.hibernateService = hibernateService;
        this.roleInterfaceClass = roleInterfaceClass;
    }

    private String encryptPassword(String password, String salt) {
        return DigestUtils.shaHex(DigestUtils.shaHex(password) + salt);
    }

    public List<Resource> getAllResources() {
        AuthService.logger.debug("Fetching all resources");

        Session session = this.hibernateService.getSession();

        @SuppressWarnings("unchecked")
        List<Resource> resources = session.createCriteria(Resource.class).list();
        session.close();

        return resources;
    }

    public Resource getResourceByName(String resourceName) throws ResourceNotFoundException {
        AuthService.logger.debug("Fetching resource " + resourceName);

        Session session = this.hibernateService.getSession();

        Query query = session.createQuery("from Resource where name = :name");
        query.setString("name", resourceName);

        List result = query.list();

        try {
            if (result.size() == 0) {
                throw new ResourceNotFoundException(resourceName);
            }

            return (Resource) result.iterator().next();
        } finally {
            session.close();
        }
    }

    /* Для внешнего использования */
    @SuppressWarnings("unused")
    public List<RoleInterface> getGrantedRoles(Resource resource) throws SQLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AuthService.logger.debug("Getting roles for resource");

        Session session = this.hibernateService.getSession();

        Query query = session.createQuery("from ResourceWithRole where resource_id = :resource_id AND user_id = :user_id");
        query.setInteger("resource_id", resource.getDatabaseId());
        query.setInteger("user_id", this.logOnUser.getDatabaseId());

        @SuppressWarnings("unchecked")
        List<ResourceWithRole> queryResult = query.list();
        session.close();

        Method values = this.roleInterfaceClass.getDeclaredMethod("values");
        Object[] enumConstants = (Object[]) values.invoke(null);

        List<RoleInterface> result = new ArrayList<>();

        for (ResourceWithRole resourceWithRole : queryResult) {
            result.add((RoleInterface) enumConstants[resourceWithRole.getRole()]);
        }

        return result;
    }

    public void authResource(Resource resource, RoleInterface role) throws ResourceNotFoundException, ResourceDeniedException, SQLException {
        AuthService.logger.debug("Authorizing resource");

        Session session = this.hibernateService.getSession();

        Query query = session.createQuery("from ResourceWithRole where resource_id = :resource_id AND user_id = :user_id AND role = :role");
        query.setInteger("resource_id", resource.getDatabaseId());
        query.setInteger("user_id", this.logOnUser.getDatabaseId());
        query.setInteger("role", role.ordinal());

        try {
            List result = query.list();

            if (result.size() == 0) {
                throw new ResourceDeniedException(resource.getName(), this.logOnUser.getLogin());
            }

            ResourceWithRole resourceWithRole = (ResourceWithRole) result.iterator().next();

            AccountingResource accountingResource = new AccountingResource();
            accountingResource.setAccounting(this.logOnUserAccounting);
            accountingResource.setResourceWithRole(resourceWithRole);

            this.logOnUserAccounting.getResources().add(accountingResource);
        } finally {
            session.close();
        }

        AuthService.logger.debug("Authorized on resource " + resource.getName());
    }

    public void authUser(String userName, String password) throws UserNotFoundException, IncorrectPasswordException, SQLException {
        AuthService.logger.debug("Authorizing user");

        Session session = this.hibernateService.getSession();

        Query query = session.createQuery("from User where login = :login");
        query.setString("login", userName);

        User user;

        try {
            List result = query.list();

            if (result.size() == 0) {
                throw new UserNotFoundException(userName);
            }

            user = (User) result.iterator().next();
        } finally {
            session.close();
        }

        // Checking password
        if (!this.encryptPassword(password, user.getSalt()).equals(user.getPasswordHash())) {
            throw new IncorrectPasswordException(userName, password);
        }

        this.logOnUser = user;
        this.logOnUserAccounting = new Accounting();
        this.logOnUserAccounting.setUser(this.logOnUser);

        AuthService.logger.debug("Authorized as user " + this.logOnUser.getLogin() + " (" + this.logOnUser.getPersonName() + ")");
    }

    public void saveAccounting() throws SQLException {
        if (this.logOnUserAccounting.getLogoutDate() == null) {
            this.logOnUserAccounting.setLogoutDate(Calendar.getInstance().getTime());
            AuthService.logger.debug("Logout date == null, setting up to current time");
        }

        AuthService.logger.debug("Saving accounting");

        Session session = this.hibernateService.getSession();

        session.beginTransaction();

        session.save(this.logOnUserAccounting);
        this.logOnUserAccounting.getResources().forEach(session::save);

        session.getTransaction().commit();
        session.close();

        this.clearAll();
    }

    public void clearAll() {
        this.logOnUserAccounting = null;
        this.logOnUser = null;
    }
}

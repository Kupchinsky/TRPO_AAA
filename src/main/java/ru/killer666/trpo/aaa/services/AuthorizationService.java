package ru.killer666.trpo.aaa.services;

import lombok.NonNull;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.killer666.trpo.aaa.InjectLogger;
import ru.killer666.trpo.aaa.domains.Resource;
import ru.killer666.trpo.aaa.domains.ResourceWithRole;
import ru.killer666.trpo.aaa.domains.User;
import ru.killer666.trpo.aaa.exceptions.IncorrectPasswordException;
import ru.killer666.trpo.aaa.exceptions.ResourceDeniedException;
import ru.killer666.trpo.aaa.exceptions.ResourceNotFoundException;
import ru.killer666.trpo.aaa.exceptions.UserNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthorizationService {
    @InjectLogger
    private static Logger logger;

    @Autowired
    private RoleResolverService roleResolverService;

    private String encryptPassword(String password, String salt) {
        return DigestUtils.shaHex(DigestUtils.shaHex(password) + salt);
    }

    public List<Resource> getAllResources(@NonNull Session session) {
        logger.debug("Fetching all resources");

        @SuppressWarnings("unchecked")
        List<Resource> resources = session.createCriteria(Resource.class).list();

        return resources;
    }

    public Resource findResourceByName(@NonNull Session session, @NonNull String resourceName) throws ResourceNotFoundException {
        logger.debug("Fetching resource " + resourceName);

        Criteria criteria = session.createCriteria(Resource.class);
        criteria.add(Expression.eq("name", resourceName));

        List result = criteria.list();

        if (result.size() == 0) {
            throw new ResourceNotFoundException(resourceName);
        }

        return (Resource) result.iterator().next();
    }

    public List<Enum<?>> findGrantedRolesForResource(@NonNull Session session, @NonNull User user, @NonNull Resource resource) {
        logger.debug("Getting granted roles for resource");

        Criteria criteria = session.createCriteria(ResourceWithRole.class);
        criteria.add(Expression.eq("resource", resource));
        criteria.add(Expression.eq("user", user));

        @SuppressWarnings("unchecked")
        List<ResourceWithRole> queryResult = criteria.list();
        List<Enum<?>> result = new ArrayList<>();

        for (ResourceWithRole resourceWithRole : queryResult) {
            result.add(this.roleResolverService.resolve(resourceWithRole.getRole()));
        }

        return result;
    }

    public ResourceWithRole authorizeOnResource(@NonNull Session session, @NonNull User user, @NonNull Resource resource,
                                                @NonNull Enum<?> role) throws ResourceNotFoundException, ResourceDeniedException {
        logger.debug("Authorizing on resource");

        Criteria criteria = session.createCriteria(ResourceWithRole.class);
        criteria.add(Expression.eq("resource", resource));
        criteria.add(Expression.eq("user", user));
        criteria.add(Expression.eq("role", role.ordinal()));

        List queryResult = criteria.list();

        if (queryResult.size() == 0) {
            throw new ResourceDeniedException(resource.getName(), user.getUserName());
        }

        ResourceWithRole result = (ResourceWithRole) queryResult.iterator().next();

        AuthorizationService.logger.debug("Authorized on resource {}", resource.getName());
        return result;
    }

    public User authorizeUser(@NonNull Session session, @NonNull String userName, @NonNull String password) throws UserNotFoundException, IncorrectPasswordException {
        logger.debug("Authorizing user");

        Criteria criteria = session.createCriteria(User.class);
        criteria.add(Expression.eq("userName", userName));

        List result = criteria.list();

        if (result.size() == 0) {
            throw new UserNotFoundException(userName);
        }

        User user = (User) result.iterator().next();

        if (!this.encryptPassword(password, user.getSalt()).equals(user.getPasswordHash())) {
            throw new IncorrectPasswordException(userName, password);
        }

        logger.debug("Authorized as user {}, ({})", user.getUserName(), user.getPersonName());
        return user;
    }
}

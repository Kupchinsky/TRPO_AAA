package ru.killer666.trpo.aaa.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import ru.killer666.trpo.aaa.domains.*;

import java.util.Properties;

public class HibernateService {

    private static final Logger logger = LogManager.getLogger(HibernateService.class);

    private SessionFactory concreteSessionFactory;

    public HibernateService(String url, String userName, String password, String dialect, Class... annotatedClasses) {

        try {
            Properties prop = new Properties();
            prop.setProperty("hibernate.hbm2ddl.auto", "update");
            prop.setProperty("hibernate.connection.url", url);
            prop.setProperty("hibernate.connection.username", userName);
            prop.setProperty("hibernate.connection.password", password);
            prop.setProperty("dialect", dialect);

            AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration()
                    .addPackage("ru.killer666.trpo.aaa.domains")
                    .addProperties(prop);

            for (Class cls : annotatedClasses) {
                annotationConfiguration.addAnnotatedClass(cls);
                HibernateService.logger.debug("Added custom annotated class: " + cls.getSimpleName());
            }

            annotationConfiguration.addAnnotatedClass(User.class)
                    .addAnnotatedClass(Resource.class)
                    .addAnnotatedClass(ResourceWithRole.class)
                    .addAnnotatedClass(Accounting.class)
                    .addAnnotatedClass(AccountingResource.class);

            this.concreteSessionFactory = annotationConfiguration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public Session getSession()
            throws HibernateException {
        HibernateService.logger.debug("Opening session");
        return this.concreteSessionFactory.openSession();
    }
}
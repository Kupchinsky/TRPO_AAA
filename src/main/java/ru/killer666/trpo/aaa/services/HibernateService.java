package ru.killer666.trpo.aaa.services;

import org.flywaydb.core.Flyway;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

import java.util.Properties;

public class HibernateService {
    private SessionFactory concreteSessionFactory;

    HibernateService(String url, String userName, String password, String dialect) {
        Flyway flyway = new Flyway();
        flyway.setDataSource(url, userName, password);
        flyway.migrate();

        try {
            Properties prop = new Properties();
            prop.setProperty("hibernate.connection.url", url);
            prop.setProperty("hibernate.connection.username", userName);
            prop.setProperty("hibernate.connection.password", password);
            prop.setProperty("dialect", dialect);

            concreteSessionFactory = new AnnotationConfiguration()
                    .addPackage("com.rosteh.invsite.core.domains")
                    .addProperties(prop)
                    .addAnnotatedClass(User.class)
                    .buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession()
            throws HibernateException {
        return concreteSessionFactory.openSession();
    }
}
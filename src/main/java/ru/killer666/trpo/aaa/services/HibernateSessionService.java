package ru.killer666.trpo.aaa.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.FactoryBean;

public interface HibernateSessionService extends FactoryBean<SessionFactory> {
}

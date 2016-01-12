package ru.killer666.trpo.aaa.services;

import lombok.NonNull;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.killer666.trpo.aaa.InjectLogger;
import ru.killer666.trpo.aaa.domains.Accounting;
import ru.killer666.trpo.aaa.domains.User;

import java.util.Calendar;

@Service
@Transactional
public class AccountingService {
    @InjectLogger
    private static Logger logger;

    @Autowired
    private SessionFactory sessionFactory;

    public Accounting createForUser(@NonNull User user) {
        Accounting accounting = new Accounting();
        accounting.setUser(user);

        return accounting;
    }

    public void save(@NonNull Accounting accounting) {
        if (accounting.getLogoutDate() == null) {
            accounting.setLogoutDate(Calendar.getInstance().getTime());
            logger.debug("Logout date == null, setting up to current time");
        }

        logger.debug("Saving accounting");

        Session session = this.sessionFactory.getCurrentSession();

        session.save(accounting);
        accounting.getResources().forEach(session::save);
    }
}

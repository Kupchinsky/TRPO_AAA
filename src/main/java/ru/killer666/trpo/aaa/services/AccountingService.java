package ru.killer666.trpo.aaa.services;

import lombok.NonNull;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import ru.killer666.trpo.aaa.InjectLogger;
import ru.killer666.trpo.aaa.domains.Accounting;
import ru.killer666.trpo.aaa.domains.User;

import java.util.Calendar;

@Service
public class AccountingService {
    @InjectLogger
    private static Logger logger;

    public Accounting createForUser(@NonNull User user) {
        Accounting accounting = new Accounting();
        accounting.setUser(user);

        return accounting;
    }

    public void save(@NonNull Session session, @NonNull Accounting accounting) {
        if (accounting.getLogoutDate() == null) {
            accounting.setLogoutDate(Calendar.getInstance().getTime());
            logger.debug("Logout date == null, setting up to current time");
        }

        logger.debug("Saving accounting");

        Transaction tx = session.beginTransaction();

        session.save(accounting);
        accounting.getResources().forEach(session::save);

        tx.commit();
    }
}
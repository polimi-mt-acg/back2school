package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.List;

@Entity
public class SeedEntityNotificationRead extends SeedDummy {

    @Transient
    public String seedUserEmail;

    @Transient
    public String seedNotificationSubject;

    @Override
    public void prepareToPersist() {
        super.prepareToPersist();
        seedAssociateNotificationToUser();
    }

    private void seedAssociateNotificationToUser() {
//        DatabaseHandler dhi = DatabaseHandler.getInstance();
//        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedUserEmail);
//        List<Notification> notifications = dhi.getListSelectFromWhereEqual(Notification.class, Notification_.subject, seedNotificationSubject);
//
//        if (users != null && notifications != null && users.size() > 0 && notifications.size() > 0) {
//            User user = users.get(0);
//            Notification notification = notifications.get(0);
//
//            user.addNotificationsRead(notification);
//
//            Session session = DatabaseHandler.getInstance().getNewSession();
//            session.beginTransaction();
//            session.persist(user);
//            session.getTransaction().commit();
//            session.close();
//        }
    }
}

package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;

@Entity
public class SeedEntityNotificationRead extends SeedDummy {

  @Transient public String seedUserEmail;

  @Transient public String seedNotificationSubject;

  @Override
  public void prepareToPersist() {
    super.prepareToPersist();
    seedAssociateNotificationToUser();
  }

  private void seedAssociateNotificationToUser() {
    Session session = DatabaseHandler.getInstance().getNewSession();

    session.beginTransaction();

    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, seedUserEmail, session);

    Optional<Notification> notificationOpt =
        DatabaseHandler.fetchEntityBy(
            Notification.class, Notification_.subject, seedNotificationSubject, session);

    // if the user is not found into the database
    if (!userOpt.isPresent()) {
      print(
          "WARNING! notifications_read.json found seedUserEmail: ",
          seedUserEmail,
          " BUT there IS NO corresponding user in the database.");
      print("NOTIFICATION-READ relation NOT created!");
      return;
    }

    // if the notification is not found into the database
    if (!notificationOpt.isPresent()) {
      print(
          "WARNING! notifications_read.json found seedNotificationSubject: ",
          seedNotificationSubject,
          " BUT there IS NO corresponding user in the database.");
      print("NOTIFICATION-READ relation NOT created!");
      return;
    }

    User user = userOpt.get();
    Notification notification = notificationOpt.get();

    user.addNotificationsRead(notification);
    session.getTransaction().commit();
    session.close();
  }
}

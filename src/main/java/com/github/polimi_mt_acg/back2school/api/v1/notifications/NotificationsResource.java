package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession_;
import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.Session;

@Path("notifications")
public class NotificationsResource {
  @GET
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public NotificationsResponse getNotifications() {
    // Get the list of notifications in the database
    List<Notification> notifications =
        DatabaseHandler.getInstance().getListSelectFrom(Notification.class);

    return new NotificationsResponse(notifications);
  }

  @Path("send-to-teachers")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToTeachers(
      NotificationGeneralTeachers notification, @Context HttpHeaders hh) {

    notification = sendNotificationTo(notification, hh);

    return Response.ok(notification, MediaType.APPLICATION_JSON).build();
  }

  @Path("send-to-parents")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToParents(
      NotificationGeneralParents notification, @Context HttpHeaders hh) {

    notification = sendNotificationTo(notification, hh);

    return Response.ok(notification, MediaType.APPLICATION_JSON).build();
  }

  private <T extends Notification> T sendNotificationTo(T notification, HttpHeaders hh) {
    // Query the notification creator
    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    String token = hh.getHeaderString(HttpHeaders.AUTHORIZATION);
    session.beginTransaction();
    User creator = getCreator(token, dhi, session);

    // Fill creator field and persist it
    notification.setCreator(creator);
    session.persist(notification);
    session.getTransaction().commit();
    session.close();

    return notification;
  }

  private User getCreator(String token, DatabaseHandler dhi, Session session) {
    List<AuthenticationSession> results =
        dhi.getListSelectFromWhereEqual(
            AuthenticationSession.class, AuthenticationSession_.token, token, session);
    return results.get(0).getUser();
  }
}

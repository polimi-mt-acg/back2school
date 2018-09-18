package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
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

  @GET
  @Path("{id: [0-9]+}")
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getNotificationById(@PathParam("id") String id) {
    // TODO
    return Response.ok().build();
  }

  @Path("send-to-teachers")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToTeachers(
      NotificationGeneralTeachers notification, @Context ContainerRequestContext crc) {

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(crc);

    // Persist the notification
    saveNotificationWithCreator(notification, creator);

    return Response.ok(notification, MediaType.APPLICATION_JSON).build();
  }

  @Path("send-to-parents")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToParents(
      NotificationGeneralParents notification, @Context ContainerRequestContext crc) {

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(crc);

    // Persist the notification
    saveNotificationWithCreator(notification, creator);

    return Response.ok(notification, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Associate the creator user to the notification and save the latter.
   *
   * @param notification The notification to which associate the user.
   * @param creator The creator user.
   */
  private void saveNotificationWithCreator(Notification notification, User creator) {
    // Fill creator field and persist it
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();
    notification.setCreator(creator);
    session.persist(notification);
    session.getTransaction().commit();
    session.close();
  }
}

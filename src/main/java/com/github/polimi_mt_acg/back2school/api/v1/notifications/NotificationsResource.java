package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.hibernate.Session;

@Path("notifications")
public class NotificationsResource {

  @Context UriInfo uriInfo;

  @GET
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getNotifications(@Context UriInfo uriInfo) {
    // Get notifications from DB
    List<Notification> notifications =
        DatabaseHandler.getInstance().getListSelectFrom(Notification.class);

    // For each user, build a URI to /students/{id}
    List<URI> uris = new ArrayList<>();
    for (Notification notification : notifications) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(notification.getId())).build();
      uris.add(uri);
    }

    NotificationsResponse response = new NotificationsResponse();
    response.setNotifications(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getNotificationById(@PathParam("id") Integer notificationId) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    Notification notification = session.get(Notification.class, notificationId);

    if (notification == null) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown notification id"))
          .build();
    }

    return Response.ok(notification, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("send-to-teachers")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToTeachers(
      NotificationGeneralTeachers notification, @Context HttpHeaders httpHeaders) {
    if (!notification.isValidForPost()) return notification.getInvalidPostResponse();

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(httpHeaders);

    // Persist the notification
    saveNotificationWithCreator(notification, creator);

    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(NotificationsResource.class)
            .path(NotificationsResource.class, "getNotificationById")
            .build(notification.getId());
    return Response.created(uri).entity(new StatusResponse(Response.Status.CREATED)).build();
  }

  @Path("send-to-parents")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToParents(
      NotificationGeneralParents notification, @Context HttpHeaders httpHeaders) {
    if (!notification.isValidForPost()) return notification.getInvalidPostResponse();

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(httpHeaders);

    // Persist the notification
    saveNotificationWithCreator(notification, creator);

    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(NotificationsResource.class)
            .path(NotificationsResource.class, "getNotificationById")
            .build(notification.getId());
    return Response.created(uri).entity(new StatusResponse(Response.Status.CREATED)).build();
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

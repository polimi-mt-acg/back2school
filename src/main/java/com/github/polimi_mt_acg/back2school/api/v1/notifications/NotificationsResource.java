package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("notifications")
public class NotificationsResource {
  @GET
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public NotificationsResponse getNotifications( ) throws JsonProcessingException {
    // Get the list of notifications in the database
    List<Notification> notifications =
        DatabaseHandler.getInstance().getListSelectFrom(Notification.class);

    return new NotificationsResponse(notifications);
  }
}

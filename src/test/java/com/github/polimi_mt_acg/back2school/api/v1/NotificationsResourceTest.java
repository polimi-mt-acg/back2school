package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class NotificationsResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioNotifications");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.notifications");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  public void getNotifications() throws IOException {
    // Get Database seeds
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioNotifications", "users.json");

    // For each administrator
    for (User admin : admins) {
      if (admin.getRole() == Role.ADMINISTRATOR) {
        // Create a get request
        Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(new String[] {"notifications"}, admin)
                .buildGet();

        // Invoke the request
        NotificationsResponse response = request.invoke(NotificationsResponse.class);
        assertNotNull(response);

        // Print it
        ObjectMapper mapper = RestFactory.objectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
      }
    }
  }

  @Test
  public void sendNotificationToTeachers() throws JsonProcessingException {
    // Get an admin
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioNotifications", "users.json");
    admins =
        admins
            .stream()
            .filter(user -> user.getRole() == Role.ADMINISTRATOR)
            .collect(Collectors.toList());
    User admin = admins.get(0);

    // Create a custom notification
    NotificationGeneralTeachers notification = makeGeneralTeachersNotification();

    // Set the POST request
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                new String[] {"notifications", "send-to-teachers"}, admin)
            .buildPost(Entity.json(notification));

    // Invoke the request
    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    NotificationGeneralTeachers responseNotif =
        response.readEntity(NotificationGeneralTeachers.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNotif));

    assertNotNull(responseNotif);
    assertEquals(responseNotif.getSubject(), notification.getSubject());
    assertEquals(responseNotif.getText(), notification.getText());
    assertEquals(responseNotif.getDatetime(), notification.getDatetime());
    assertEquals(responseNotif.getCreator().getEmail(), admin.getEmail());
  }

  @Test
  public void sendNotificationToParents() throws JsonProcessingException {
    // Get an admin
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioNotifications", "users.json");

    admins =
        admins
            .stream()
            .filter(user -> user.getRole() == Role.ADMINISTRATOR)
            .collect(Collectors.toList());
    User admin = admins.get(0);

    // Create a custom notification
    NotificationGeneralParents notification = makeGeneralParentsNotification();

    // Create the POST request
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
            new String[] {"notifications", "send-to-parents"}, admin)
            .buildPost(Entity.json(notification));

    // Invoke the request
    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    NotificationGeneralParents responseNotif =
        response.readEntity(NotificationGeneralParents.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseNotif));

    assertNotNull(responseNotif);
    assertEquals(responseNotif.getSubject(), notification.getSubject());
    assertEquals(responseNotif.getText(), notification.getText());
    assertEquals(responseNotif.getDatetime(), notification.getDatetime());
    assertEquals(responseNotif.getCreator().getEmail(), admin.getEmail());
  }

  private NotificationGeneralTeachers makeGeneralTeachersNotification() {
    NotificationGeneralTeachers notification = new NotificationGeneralTeachers();
    notification.setSubject("Test subject");
    notification.setText("Test notification text");
    notification.setDatetime(LocalDateTime.now());
    return notification;
  }

  private NotificationGeneralParents makeGeneralParentsNotification() {
    NotificationGeneralParents notification = new NotificationGeneralParents();
    notification.setSubject("Test subject");
    notification.setText("Test notification text");
    notification.setDatetime(LocalDateTime.now());
    return notification;
  }
}

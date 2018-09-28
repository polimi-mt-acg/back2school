package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NotificationsResourceTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioNotifications");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.notifications",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioSubjects", User.Role.ADMINISTRATOR);
    assertNotNull(adminForAuth);
  }

  @AfterClass
  public static void tearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getNotifications() {
    List<URI> notificationsURIs =
        RestFactory.doGetRequest(adminForAuth, "notifications")
            .readEntity(NotificationsResponse.class)
            .getNotifications();

    print("GET /notifications");
    for (URI uri : notificationsURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void sendNotificationToTeachers() {
    NotificationGeneralTeachers newNotification = buildGeneralTeachersNotification();

    // Set the POST request
    URI notificationURI =
        RestFactory.doPostRequest(
            adminForAuth, newNotification, "notifications", "send-to-teachers");

    // get the created entity
    NotificationGeneralTeachers notificationResp =
        RestFactory.doGetRequest(adminForAuth, notificationURI)
            .readEntity(NotificationGeneralTeachers.class);

    assertNotNull(notificationResp);
    assertEquals(newNotification.getSubject(), notificationResp.getSubject());
    assertEquals(newNotification.getText(), notificationResp.getText());

    print("GET ", notificationURI);
    print(notificationResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void sendNotificationToParents() {
    NotificationGeneralParents newNotification = buildGeneralParentsNotification();

    // Set the POST request
    URI notificationURI =
        RestFactory.doPostRequest(
            adminForAuth, newNotification, "notifications", "send-to-parents");

    // get the created entity
    NotificationGeneralParents notificationResp =
        RestFactory.doGetRequest(adminForAuth, notificationURI)
            .readEntity(NotificationGeneralParents.class);

    assertNotNull(notificationResp);
    assertEquals(newNotification.getSubject(), notificationResp.getSubject());
    assertEquals(newNotification.getText(), notificationResp.getText());

    print("GET ", notificationURI);
    print(notificationResp);
  }

  private NotificationGeneralTeachers buildGeneralTeachersNotification() {
    NotificationGeneralTeachers notification = new NotificationGeneralTeachers();
    notification.setSubject("Test subject");
    notification.setText("Test notification text");
    return notification;
  }

  private NotificationGeneralParents buildGeneralParentsNotification() {
    NotificationGeneralParents notification = new NotificationGeneralParents();
    notification.setSubject("Test subject");
    notification.setText("Test notification text");
    return notification;
  }
}

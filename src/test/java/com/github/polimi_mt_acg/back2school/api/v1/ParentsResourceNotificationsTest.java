package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.model.NotificationPersonalParent;
import com.github.polimi_mt_acg.back2school.model.User;
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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.buildMarcos;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.*;

public class ParentsResourceNotificationsTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Truncate DB
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.parents",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioParents", User.Role.ADMINISTRATOR);
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
  public void getParentNotificationsFromAdmin() {
    User parent = buildMarcos(1);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    NotificationsResponse parentNotifications =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "notifications")
            .readEntity(NotificationsResponse.class);

    // Print it
    print("GET /parents/", parentID, "/notifications");
    print(parentNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentNotificationsFromAdmin() {
    User parent = buildMarcos(2);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    NotificationPersonalParent postParentNotification = buildNotification(1);
    RestFactory.doPostRequest(
        adminForAuth, postParentNotification, "parents", parentID, "notifications");

    // Now query /parents/{parent_id}/notifications from admin
    NotificationsResponse parentNotifications =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "notifications")
            .readEntity(NotificationsResponse.class);

    assertTrue(parentNotifications.getNotifications().size() > 0);

    // Print it
    print(parentNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromAdministrator() {
    User parent = buildMarcos(3);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copyNumber = 2;

    NotificationPersonalParent postParentNotification = buildNotification(copyNumber);
    RestFactory.doPostRequest(
        adminForAuth, postParentNotification, "parents", parentID, "notifications");

    NotificationsResponse parentNotifications =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "notifications")
            .readEntity(NotificationsResponse.class);

    for (URI notURI : parentNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Notification notification =
          RestFactory.doGetRequest(
                  adminForAuth, "parents", parentID, "notifications", notificationID)
              .readEntity(Notification.class);

      // Print it
      print(notification);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromSameParent() {
    User parent = buildMarcos(4);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copyNumber = 2;

    NotificationPersonalParent postParentNotification = buildNotification(copyNumber);
    RestFactory.doPostRequest(
        adminForAuth, postParentNotification, "parents", parentID, "notifications");

    NotificationsResponse parentNotifications =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "notifications")
            .readEntity(NotificationsResponse.class);

    for (URI notURI : parentNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Notification notificationResp =
          RestFactory.doGetRequest(parent, "parents", parentID, "notifications", notificationID)
              .readEntity(Notification.class);

      // Print it
      print(notificationResp);
    }
  }

  private NotificationPersonalParent buildNotification(int copynumber) {
    NotificationPersonalParent notification = new NotificationPersonalParent();
    notification.setSubject("Subject number: " + String.valueOf(copynumber));
    notification.setText("Text number: " + String.valueOf(copynumber));
    return notification;
  }
}

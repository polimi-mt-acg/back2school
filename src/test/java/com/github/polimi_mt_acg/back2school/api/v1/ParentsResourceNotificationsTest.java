package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.buildMarcos;
import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.doParentPost;
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
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "notifications")
            .buildGet();

    Response response = request.invoke();
    print("GET /parents/", parentID, "/notifications");
    print(response.toString());

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    NotificationsResponse parentNotifications = response.readEntity(NotificationsResponse.class);

    // Print it
    print(parentNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentNotificationsFromAdmin() {
    User parent = buildMarcos(2);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    postNotification(1, parentID);

    // Now query /parents/{parent_id}/notifications from admin
    Invocation requestCheck =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "notifications")
            .buildGet();

    Response responseCheck = requestCheck.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());

    NotificationsResponse parentNotifications =
        responseCheck.readEntity(NotificationsResponse.class);

    assertTrue(parentNotifications.getNotifications().size() > 0);

    // Print it
    print(parentNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromAdministrator() {
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copynumber = 2;

    postNotification(copynumber, parentID);

    Invocation requestGET =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "notifications")
            .buildGet();
    Response responseGET = requestGET.invoke();
    NotificationsResponse parentNotifications = responseGET.readEntity(NotificationsResponse.class);

    for (URI notURI : parentNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Invocation requestGetNotificationByID =
          RestFactory.getAuthenticatedInvocationBuilder(
                  adminForAuth, "parents", parentID, "notifications", notificationID)
              .buildGet();

      Response response = requestGetNotificationByID.invoke();

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Notification notificationResp = response.readEntity(Notification.class);

      // Print it
      print(notificationResp);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromSameParent() {
    User parent = buildMarcos(4);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copyNumber = 2;

    postNotification(copyNumber, parentID);

    Invocation requestGET =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "notifications")
            .buildGet();
    Response responseGET = requestGET.invoke();
    NotificationsResponse parentNotifications = responseGET.readEntity(NotificationsResponse.class);

    for (URI notURI : parentNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Invocation requestGetNotificationByID =
          RestFactory.getAuthenticatedInvocationBuilder(
                  parent, "parents", parentID, "notifications", notificationID)
              .buildGet();

      Response response = requestGetNotificationByID.invoke();

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Notification notificationResp = response.readEntity(Notification.class);

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

  private URI postNotification(int copyNumber, String parentID) {
    // We post a notification between parent and admin
    NotificationPersonalParent postParentNotification = buildNotification(copyNumber);

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "notifications")
            .buildPost(Entity.json(postParentNotification));

    Response response = request.invoke();
    System.out.println(response.toString());
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

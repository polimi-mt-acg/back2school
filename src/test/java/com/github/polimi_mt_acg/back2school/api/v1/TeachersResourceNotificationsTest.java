package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.model.*;
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

import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;
import static org.junit.Assert.*;

public class TeachersResourceNotificationsTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioTeachers");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.teachers",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");

    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.ADMINISTRATOR);
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
  public void getTeacherNotificationsFromAdmin() {
    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);

    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacher.getId(), "notifications")
            .readEntity(NotificationsResponse.class);

    assertEquals(4, teacherNotifications.getNotifications().size());

    // Print it
    print("GET /parents/", teacher.getId(), "/notifications  -- from admin");
    print(teacherNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotificationsFromTeacher() {
    // Get teacher from seed
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);

    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);
    assertTrue(teacher.weakEquals(seedTeacher));

    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(seedTeacher, "teachers", teacher.getId(), "notifications")
            .readEntity(NotificationsResponse.class);

    assertEquals(4, teacherNotifications.getNotifications().size());

    // Print it
    print("GET /teachers/", teacher.getId(), "/notifications  -- from teacher");
    print(teacherNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeacherNotificationsFromAdmin() {
    // Get teacher from seed
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, seedTeacher, "teachers");
    Path fullPath = Paths.get("/", teacherURI.getPath());
    String teacherID = fullPath.getParent().relativize(fullPath).toString();

    NotificationPersonalTeacher postTeacherNotification = buildNotification(0);
    RestFactory.doPostRequest(
        adminForAuth, postTeacherNotification, "teachers", teacherID, "notifications");

    // Now query /teachers/{teacherId}/notifications from admin
    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacherID, "notifications")
            .readEntity(NotificationsResponse.class);

    // 2: one is GENERAL-TEACHERS (scenarioTeachers) and the other is the inserted above
    assertEquals(2, teacherNotifications.getNotifications().size());

    // Print it
    print("GET /teachers/", teacherID, "/notifications");
    print(teacherNotifications);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotificationsByIdFromAdministrator() {
    // Get teacher from seed
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 1);
    assertNotNull(seedTeacher);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, seedTeacher, "teachers");
    Path fullPath = Paths.get("/", teacherURI.getPath());
    String teacherID = fullPath.getParent().relativize(fullPath).toString();

    NotificationPersonalTeacher postTeacherNotification = buildNotification(1);
    RestFactory.doPostRequest(
        adminForAuth, postTeacherNotification, "teachers", teacherID, "notifications");

    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacherID, "notifications")
            .readEntity(NotificationsResponse.class);

    for (URI notURI : teacherNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      String notificationID = fullNotPath.getParent().relativize(fullNotPath).toString();

      Notification notification =
          RestFactory.doGetRequest(
                  adminForAuth, "teachers", teacherID, "notifications", notificationID)
              .readEntity(Notification.class);

      Notification notificationSecondTime =
          RestFactory.doGetRequest(
                  adminForAuth, "teachers", teacherID, "notifications", notificationID)
              .readEntity(Notification.class);

      // Print it
      print("GET /teachers/", teacherID, "/notifications/", notificationID, " -- from admin");
      print(notification);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotificationsByIdFromSameTeacher() {
    // Get teacher from seed
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 2);
    assertNotNull(seedTeacher);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, seedTeacher, "teachers");
    Path fullPath = Paths.get("/", teacherURI.getPath());
    String teacherID = fullPath.getParent().relativize(fullPath).toString();

    NotificationPersonalTeacher postTeacherNotification = buildNotification(2);
    RestFactory.doPostRequest(
        adminForAuth, postTeacherNotification, "teachers", teacherID, "notifications");

    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(seedTeacher, "teachers", teacherID, "notifications")
            .readEntity(NotificationsResponse.class);

    for (URI notURI : teacherNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      String notificationID = fullNotPath.getParent().relativize(fullNotPath).toString();

      Notification notification =
          RestFactory.doGetRequest(
                  seedTeacher, "teachers", teacherID, "notifications", notificationID)
              .readEntity(Notification.class);

      // Print it
      print("GET /teachers/", teacherID, "/notifications/", notificationID, " -- from teacher");
      print(notification);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotificationsByIdFromOtherTeacher() {
    // Get teacher from seed
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 3);
    assertNotNull(seedTeacher);

    User seedTeacherForRequest =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 4);
    assertNotNull(seedTeacherForRequest);
    RestFactory.doPostRequest(adminForAuth, seedTeacherForRequest, "teachers");

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, seedTeacher, "teachers");
    Path fullPath = Paths.get("/", teacherURI.getPath());
    String teacherID = fullPath.getParent().relativize(fullPath).toString();

    NotificationPersonalTeacher postTeacherNotification = buildNotification(3);
    RestFactory.doPostRequest(
        adminForAuth, postTeacherNotification, "teachers", teacherID, "notifications");

    NotificationsResponse teacherNotifications =
        RestFactory.doGetRequest(seedTeacher, "teachers", teacherID, "notifications")
            .readEntity(NotificationsResponse.class);

    for (URI notURI : teacherNotifications.getNotifications()) {
      Path fullNotPath = Paths.get("/", notURI.getPath());
      String notificationID = fullNotPath.getParent().relativize(fullNotPath).toString();

      Response getResponse =
          RestFactory.getAuthenticatedInvocationBuilder(
                  seedTeacherForRequest, "teachers", teacherID, "notifications", notificationID)
              .buildGet()
              .invoke();
      assertEquals(Response.Status.FORBIDDEN.getStatusCode(), getResponse.getStatus());
    }
  }

  private NotificationPersonalTeacher buildNotification(int copynumber) {
    NotificationPersonalTeacher notification = new NotificationPersonalTeacher();
    notification.setSubject("Subject number: " + String.valueOf(copynumber));
    notification.setText("Text number: " + String.valueOf(copynumber));
    return notification;
  }
}

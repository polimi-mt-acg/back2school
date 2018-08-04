package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
import com.github.polimi_mt_acg.back2school.model.Appointment;
import com.github.polimi_mt_acg.back2school.model.Notification;
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
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ParentResourceNotificationsTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Truncate DB
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
            HTTPServerManager.startServer(
                    AuthenticationResource.class, "com.github.polimi_mt_acg.back2school.api.v1.parents",
                    "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsFromAdmin() throws JsonProcessingException {
    User parent = buildMarcos(1);
    URI parentURI = doParentPost(0,parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin = get(User.Role.ADMINISTRATOR);

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "notifications").buildGet();

    Response response = request.invoke();
    System.out.println("HERE 2"+response.toString());

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ParentNotificationsResponse parentNotifications = response.readEntity(ParentNotificationsResponse.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentNotifications));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentNotificationsFromAdmin() throws JsonProcessingException {
    User parent = buildMarcos(2);
    URI parentURI = doParentPost(1, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin = get(User.Role.ADMINISTRATOR);

    URI notificationURI = postNotification(1,parentID);

    // Now query /parents/{parent_id}/notifications from admin
    Invocation requestCheck =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "notifications")
                    .buildGet();

    Response responseCheck = requestCheck.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());

    ParentNotificationsResponse parentNotifications = responseCheck.readEntity(ParentNotificationsResponse.class);

    assertTrue(parentNotifications.getNotifications().size() > 0);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentNotifications));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromAdministrator() throws JsonProcessingException {
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(0, parent);
    User admin = get(User.Role.ADMINISTRATOR);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copynumber=2;

    URI notificationPPURI = postNotification(copynumber,parentID);

    Invocation requestGET =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "notifications").buildGet();
    Response responseGET = requestGET.invoke();
    ParentNotificationsResponse parentNotifications = responseGET.readEntity(ParentNotificationsResponse.class);

    for(URI notURI : parentNotifications.getNotifications()){
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Invocation requestGetNotificationByID =
              RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID,"notifications", notificationID).buildGet();

      Response response = requestGetNotificationByID.invoke();
//      System.out.println("HERE1"+response);

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Notification notificationResp = response.readEntity(Notification.class);

//      assertTrue( notificationResp.getSubject().equals("Subject number: "+ String.valueOf(copynumber)));
//      assertTrue( notificationResp.getText().equals("Text number: "+ String.valueOf(copynumber)));

      // Print it
      ObjectMapper mapper = RestFactory.objectMapper();
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notificationResp));

    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentNotificationsByIdFromSameParent() throws JsonProcessingException {
    User parent = buildMarcos(4);
    URI parentURI = doParentPost(0, parent);
    User admin = get(User.Role.ADMINISTRATOR);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int copynumber=2;

    URI notificationPPURI = postNotification(copynumber,parentID);

    Invocation requestGET =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "notifications").buildGet();
    Response responseGET = requestGET.invoke();
    ParentNotificationsResponse parentNotifications = responseGET.readEntity(ParentNotificationsResponse.class);

    for(URI notURI : parentNotifications.getNotifications()){
      Path fullNotPath = Paths.get("/", notURI.getPath());
      Path idNotPath = fullNotPath.getParent().relativize(fullNotPath);
      String notificationID = idNotPath.toString();

      Invocation requestGetNotificationByID =
              RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID,"notifications", notificationID).buildGet();

      Response response = requestGetNotificationByID.invoke();
//      System.out.println("HERE1"+response);

      assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      Notification notificationResp = response.readEntity(Notification.class);

//      assertTrue( notificationResp.getSubject().equals("Subject number: "+ String.valueOf(copynumber)));
//      assertTrue( notificationResp.getText().equals("Text number: "+ String.valueOf(copynumber)));

      // Print it
      ObjectMapper mapper = RestFactory.objectMapper();
      System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(notificationResp));
    }
  }

  private User get(User.Role role) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithRole =
            users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private User buildMarcos(int copyNumber) {
    User marcos = new User();
    marcos.setName("Marcos " + copyNumber);
    marcos.setSurname("Ferdinand " + copyNumber);
    marcos.setEmail("marcos.ferdinand" + copyNumber + "@mail.com");
    marcos.setSeedPassword("marcos_password");
    marcos.setRole(User.Role.PARENT);
    marcos.prepareToPersist();
    return marcos;
  }

  private User getAChild(int copynumber) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");
    List<User> children =
            users
                    .stream()
                    .filter(user -> user.getRole().equals(User.Role.STUDENT))
                    .collect(Collectors.toList());
    return children.get(copynumber);
  }

  /**
   * Do a post an return the inserted parent URI.
   *
   * @return The inserted resource URI.
   */
  private URI doParentPost(int copynumber, User parent) {
    User child = getAChild(copynumber);
    String childEmail = child.getEmail();

    // Now build a PostParentRequest
    PostParentRequest request = new PostParentRequest();
    request.setParentAndPassword(parent, parent.getSeedPassword());
    request.setStudentEmail(childEmail);

    User admin = get(User.Role.ADMINISTRATOR);

    // Make a POST to /parents
    Invocation post =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents")
                    .buildPost(Entity.json(request));

    Response response = post.invoke();
    System.out.println("HERE POST REQ" + response.toString());

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  private PostParentNotificationRequest buildNotification(int copynumber, String creatorEmail) {
    PostParentNotificationRequest notification = new PostParentNotificationRequest();
    notification.setCreatorEmail(creatorEmail);
    notification.setDatetime(LocalDateTime.now());
    notification.setSubject("Subject number: "+ String.valueOf(copynumber));
    notification.setText("Text number: "+ String.valueOf(copynumber));
    return notification;
  }

  private URI postNotification( int copynumber, String parentID){

    User admin = get(User.Role.ADMINISTRATOR);
    //We post a notification between parent and admin
    PostParentNotificationRequest postParentNotificationRequest =
            buildNotification(copynumber, admin.getEmail());

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "notifications")
                    .buildPost(Entity.json(postParentNotificationRequest));

    Response response = request.invoke();
    System.out.println(response.toString());
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

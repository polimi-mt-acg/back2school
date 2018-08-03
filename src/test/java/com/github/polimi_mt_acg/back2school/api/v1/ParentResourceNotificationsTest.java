package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentAppointmentsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentNotificationsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.PostParentAppointmentRequest;
import com.github.polimi_mt_acg.back2school.api.v1.parents.PostParentRequest;
import com.github.polimi_mt_acg.back2school.model.Appointment;
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

//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void postParentAppointmentFromAdmin() throws JsonProcessingException {
//    User parent = buildMarcos(3);
//    URI parentURI = doParentPost(8, parent);
//
//    PostParentAppointmentRequest postParentAppointmentRequest =
//            buildAppointment(1,2, "carl@email.com");
//
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    User admin = get(User.Role.ADMINISTRATOR);
//
//    Invocation request =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest));
//
//    Response response = request.invoke();
//    System.out.println("HERE 2"+response.toString());
//
//    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
//
//    // Now query /parents/{parent_id}/appointments from admin
//    Invocation requestCheck =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildGet();
//
//    Response responseCheck = requestCheck.invoke();
//
//    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());
//
//    ParentAppointmentsResponse parentAppointments = responseCheck.readEntity(ParentAppointmentsResponse.class);
//
//    assertTrue(parentAppointments.getAppointments().size() > 0);
//
//    // Print it
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentAppointments));
//  }
//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void postParentAppointmentFromParent() throws JsonProcessingException {
//    User parent = buildMarcos(4);
//    URI parentURI = doParentPost(8, parent);
//
//    PostParentAppointmentRequest postParentAppointmentRequest =
//            buildAppointment(15,3, "carl@email.com");
//
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    Invocation request =
//            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest));
//
//    Response response = request.invoke();
////    System.out.println("HERE 2"+response.toString());
//
//    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
//
//    // Now query /parents/{parent_id}/appointments from admin
//    Invocation requestCheck =
//            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "appointments")
//                    .buildGet();
//
//    Response responseCheck = requestCheck.invoke();
//
//    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());
//
//    ParentAppointmentsResponse parentAppointments = responseCheck.readEntity(ParentAppointmentsResponse.class);
//
//    assertTrue(parentAppointments.getAppointments().size() > 0);
//
//    // Print it
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentAppointments));
//  }
//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void postTwoParentAppointmentsInConflictFromAdmin() throws JsonProcessingException {
//    User parent = buildMarcos(5);
//    URI parentURI = doParentPost(8, parent);
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    User admin = get(User.Role.ADMINISTRATOR);
//
//    //We post the first appointment between parent and teacher Carl
//    PostParentAppointmentRequest postParentAppointmentRequest1 =
//            buildAppointment(30,1, "carl@email.com");
//
//    Invocation request1 =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest1));
//
//    Response response1 = request1.invoke();
//    System.out.println("HERE 1"+response1.toString());
//
//    assertEquals(Response.Status.CREATED.getStatusCode(), response1.getStatus());
//
//    //Tested conflict between same parent and same teacher
//    PostParentAppointmentRequest postParentAppointmentRequest2 =
//            buildAppointment(35,1, "carl@email.com");
//
//    Invocation request2 =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest2));
//    Response response2 = request2.invoke();
//    System.out.println("HERE 2"+response2.toString());
//
//    assertEquals(Response.Status.CONFLICT.getStatusCode(), response2.getStatus());
//
//    //Tested conflict between same parent and different teacher
//    PostParentAppointmentRequest postParentAppointmentRequest3 =
//            buildAppointment(35, 1,"john@email.com");
//
//    Invocation request3 =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest3));
//    Response response3 = request3.invoke();
//    System.out.println("HERE 3"+response3.toString());
//
//    assertEquals(Response.Status.CONFLICT.getStatusCode(), response3.getStatus());
//
//    //Tested conflict between different parent and same teacher
//    User parent2 = buildMarcos(6);
//    URI parentURI2 = doParentPost(5, parent2);
//
//    PostParentAppointmentRequest postParentAppointmentRequest4 =
//            buildAppointment(30, 1,"carl@email.com");
//
//    Path fullPath2 = Paths.get("/", parentURI2.getPath());
//    Path idPath2 = fullPath2.getParent().relativize(fullPath2);
//    String parentID2 = idPath2.toString();
//
//    Invocation request4 =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID2, "appointments")
//                    .buildPost(Entity.json(postParentAppointmentRequest4));
//
//    Response response4 = request4.invoke();
//    System.out.println("HERE 4"+response4.toString());
//
//    assertEquals(Response.Status.CONFLICT.getStatusCode(), response4.getStatus());
//
//    // Now query /parents/{parent_id}/appointments from admin
//    //Only the first appointment should be retrieved by the parent
//    Invocation requestCheck =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
//                    .buildGet();
//
//    Response responseCheck = requestCheck.invoke();
//
//    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());
//
//    ParentAppointmentsResponse parentAppointments = responseCheck.readEntity(ParentAppointmentsResponse.class);
//
//    assertTrue(parentAppointments.getAppointments().size() > 0);
//
//    // Print it
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentAppointments));
//
////    System.out.println("ENDED APPOINTMENTS CONFLICT TEST");
//  }
//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void getParentAppointmentByIdFromAdministrator() throws JsonProcessingException {
//    User parent = buildMarcos(7);
//    URI parentURI = doParentPost(3, parent);
//    User admin = get(User.Role.ADMINISTRATOR);
//
//    // Now query /parents/{bob_id} from admin
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    int initialTimeApp = 10;
//    int dayApp = 5;
//
//    URI appointmentURI = postAppointment(initialTimeApp,dayApp,parentID,"carl@email.com" );
//
//    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
//    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
//    String appointmentID = idAppPath.toString();
//
//    Invocation requestGetAppointmentByID =
//            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID,"appointments", appointmentID).buildGet();
//
//    Response response = requestGetAppointmentByID.invoke();
//    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//    //    System.out.println("HERE1"+response.getStatus());
//    Appointment appointmentResp = response.readEntity(Appointment.class);
//
//    assertTrue(appointmentResp.getTeacher().getEmail().equals("carl@email.com"));
//    assertTrue(appointmentResp.getParent().getEmail().equals(parent.getEmail()));
//    assertTrue(appointmentResp.getDatetimeStart().equals(LocalDateTime.of(2018, Month.JANUARY,dayApp,12,initialTimeApp)));
//
//    // Print it
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(appointmentResp));
//  }
//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void getParentAppointmentByIdFromSameParent() throws JsonProcessingException {
//    User parent = buildMarcos(8);
//    URI parentURI = doParentPost(4, parent);
//
//    // Now query /parents/{bob_id} from admin
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    int initialTimeApp = 10;
//    int dayApp = 6;
//
//    URI appointmentURI = postAppointment(initialTimeApp,dayApp,parentID,"carl@email.com" );
//
//    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
//    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
//    String appointmentID = idAppPath.toString();
//
//    Invocation requestGetAppointmentByID =
//            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID,"appointments", appointmentID).buildGet();
//
//    Response response = requestGetAppointmentByID.invoke();
//    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//    //    System.out.println("HERE1"+response.getStatus());
//    Appointment appointmentResp = response.readEntity(Appointment.class);
//
//    assertTrue(appointmentResp.getTeacher().getEmail().equals("carl@email.com"));
//    assertTrue(appointmentResp.getParent().getEmail().equals(parent.getEmail()));
//    assertTrue(appointmentResp.getDatetimeStart().equals(LocalDateTime.of(2018, Month.JANUARY,dayApp,12,initialTimeApp)));
//
//    // Print it
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(appointmentResp));
//  }
//
//  @Test
//  @Category(TestCategory.Endpoint.class)
//  public void putParentAppointmentFromParent() throws JsonProcessingException {
//    User parent = buildMarcos(9);
//    URI parentURI = doParentPost(2, parent);
//
//    Path fullPath = Paths.get("/", parentURI.getPath());
//    Path idPath = fullPath.getParent().relativize(fullPath);
//    String parentID = idPath.toString();
//
//    URI appointmentURI =
//            postAppointment(15,13,parentID, "carl@email.com");
//
//    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
//    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
//    String appointmentID = idAppPath.toString();
//
//    int newInitialTime =20;
//    int newDay= 13;
//
//    PostParentAppointmentRequest putParentAppointmentRequest =
//            buildAppointment(newInitialTime,newDay,"carl@email.com");
//
//    Invocation requestPut =
//            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "appointments",appointmentID )
//                    .buildPut(Entity.json(putParentAppointmentRequest));
//
//    Response responsePut = requestPut.invoke();
//
//    System.out.println("HERE"+ responsePut);
//    assertEquals(Response.Status.OK.getStatusCode(), responsePut.getStatus());
//
//    // Make a new GET to compare results
//    Invocation newGetAppointment =
//            RestFactory.getAuthenticatedInvocationBuilder(parent, appointmentURI).buildGet();
//
//    Appointment newAppointmentResponse = newGetAppointment.invoke().readEntity(Appointment.class);
//
//    assertEquals(newAppointmentResponse.getParent().getEmail() , parent.getEmail());
//    assertEquals(newAppointmentResponse.getTeacher().getEmail() , "carl@email.com");
//    assertEquals(newAppointmentResponse.getDatetimeStart() , LocalDateTime.of(2018, Month.JANUARY,newDay,12,newInitialTime));
//    assertEquals(newAppointmentResponse.getDatetimeEnd() , LocalDateTime.of(2018, Month.JANUARY,newDay,12,newInitialTime+10));
//
//    ObjectMapper mapper = RestFactory.objectMapper();
//    System.out.println(
//            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newAppointmentResponse));
//  }
//


  private User get(User.Role role) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithRole =
            users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private User getUserByID(String userID) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithID =
            users
                    .stream()
                    .filter(user -> user.getId() == Integer.parseInt(userID))
                    .collect(Collectors.toList());
    return usersWithID.get(0);
  }

  private User getUserByEmail(String email) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithEmail =
            users
                    .stream()
                    .filter(user -> user.getEmail().equals(email) )
                    .collect(Collectors.toList());
    return usersWithEmail.get(0);
  }

  private User buildCarlos(int copyNumber) {
    User carlos = new User();
    carlos.setName("Carlos " + copyNumber);
    carlos.setSurname("Hernandez " + copyNumber);
    carlos.setEmail("carlos.hernandez" + copyNumber + "@mail.com");
    carlos.setSeedPassword("carlos_password");
    carlos.setRole(User.Role.STUDENT);
    carlos.prepareToPersist();
    return carlos;
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

  private boolean weakEquals(User u, User p) {
    return u.getName().equals(p.getName())
            && u.getSurname().equals(p.getSurname())
            && u.getEmail().equals(p.getEmail());
  }

  private PostParentAppointmentRequest buildAppointment(int initialTime,int day, String teacherEmail) {
    PostParentAppointmentRequest appointment = new PostParentAppointmentRequest();
    appointment.setTeacherEmail(teacherEmail);
    appointment.setDatetimeStart(LocalDateTime.of(2018, Month.JANUARY,day,12,initialTime));
    appointment.setDatetimeEnd(LocalDateTime.of(2018, Month.JANUARY,day,12,initialTime+10));
    return appointment;
  }

  private URI postAppointment(int initialTime, int day, String parentID, String teacherEmail){

    User admin = get(User.Role.ADMINISTRATOR);
    //We post an appointment between parent and teacher Carl
    PostParentAppointmentRequest postParentAppointmentRequest =
            buildAppointment(initialTime, day,teacherEmail);

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "appointments")
                    .buildPost(Entity.json(postParentAppointmentRequest));

    Response response = request.invoke();
    System.out.println(response.toString());
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }


}

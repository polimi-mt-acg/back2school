package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
import com.github.polimi_mt_acg.back2school.model.Appointment;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
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

import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.buildMarcos;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

public class ParentsResourceAppointmentsTest {

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
  public void getParentAppointmentsFromAdmin() {
    User parent = buildMarcos(1);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentAppointmentsResponse parentAppointments =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "appointments")
            .readEntity(ParentAppointmentsResponse.class);

    // Print it
    print(parentAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentAppointmentsFromSameParent() {
    User parent = buildMarcos(2);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentAppointmentsResponse parentAppointments =
        RestFactory.doGetRequest(parent, "parents", parentID, "appointments")
            .readEntity(ParentAppointmentsResponse.class);

    // Print it
    print(parentAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentAppointmentFromAdmin() {
    User parent = buildMarcos(3);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    ParentAppointmentRequest parentAppointmentRequest = buildAppointment(1, 2, "carl@email.com");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    URI uri =
        RestFactory.doPostRequest(
            adminForAuth, parentAppointmentRequest, "parents", parentID, "appointments");
    print("Created: ", uri);

    // Now query /parents/{parent_id}/appointments from admin
    ParentAppointmentsResponse parentAppointments =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "appointments")
            .readEntity(ParentAppointmentsResponse.class);

    assertTrue(parentAppointments.getAppointments().size() > 0);

    // Print it
    print(parentAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentAppointmentFromParent() {
    User parent = buildMarcos(4);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    ParentAppointmentRequest parentAppointmentRequest = buildAppointment(15, 3, "carl@email.com");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    RestFactory.doPostRequest(
        parent, parentAppointmentRequest, "parents", parentID, "appointments");

    // Now query /parents/{parent_id}/appointments from admin
    ParentAppointmentsResponse parentAppointments =
        RestFactory.doGetRequest(parent, "parents", parentID, "appointments")
            .readEntity(ParentAppointmentsResponse.class);

    assertTrue(parentAppointments.getAppointments().size() > 0);

    // Print it
    print(parentAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTwoParentAppointmentsInConflictFromAdmin() {
    User parent = buildMarcos(5);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    // We post the first appointment between parent and teacher Carl
    ParentAppointmentRequest parentAppointmentRequest1 = buildAppointment(30, 1, "carl@email.com");

    URI uri =
        RestFactory.doPostRequest(
            adminForAuth, parentAppointmentRequest1, "parents", parentID, "appointments");
    print("[1] Created: ", uri);

    // Tested conflict between same parent and same teacher
    ParentAppointmentRequest parentAppointmentRequest2 = buildAppointment(35, 1, "carl@email.com");

    Invocation request2 =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "appointments")
            .buildPost(Entity.json(parentAppointmentRequest2));
    Response response2 = request2.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response2.getStatus());

    print("[2] Conflict at: ", response2.toString());

    // Tested conflict between same parent and different teacher
    ParentAppointmentRequest parentAppointmentRequest3 = buildAppointment(35, 1, "john@email.com");

    Invocation request3 =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "appointments")
            .buildPost(Entity.json(parentAppointmentRequest3));
    Response response3 = request3.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response3.getStatus());

    print("[3] ", response3.toString());

    // Tested conflict between different parent and same teacher
    User parent2 = buildMarcos(6);
    URI parentURI2 = RestFactory.doPostRequest(adminForAuth, parent2, "parents");

    ParentAppointmentRequest parentAppointmentRequest4 = buildAppointment(30, 1, "carl@email.com");

    Path fullPath2 = Paths.get("/", parentURI2.getPath());
    Path idPath2 = fullPath2.getParent().relativize(fullPath2);
    String parentID2 = idPath2.toString();

    Invocation request4 =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID2, "appointments")
            .buildPost(Entity.json(parentAppointmentRequest4));
    Response response4 = request4.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response4.getStatus());

    print("[4] ", response4.toString());

    // Now query /parents/{parent_id}/appointments from admin
    // Only the first appointment should be retrieved by the parent
    ParentAppointmentsResponse parentAppointments =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "appointments")
            .readEntity(ParentAppointmentsResponse.class);

    assertTrue(parentAppointments.getAppointments().size() > 0);

    // Print it
    print(parentAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentAppointmentByIdFromAdministrator() {
    User parent = buildMarcos(7);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int initialTimeApp = 10;
    int dayApp = 5;

    URI appointmentURI = postAppointment(initialTimeApp, dayApp, parentID, "carl@email.com");

    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
    String appointmentID = idAppPath.toString();

    Appointment appointmentResp =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "appointments", appointmentID)
            .readEntity(Appointment.class);

    assertEquals("carl@email.com", appointmentResp.getTeacher().getEmail());
    assertEquals(appointmentResp.getParent().getEmail(), parent.getEmail());
    assertEquals(
        appointmentResp.getDatetimeStart(),
        LocalDateTime.of(2018, Month.JANUARY, dayApp, 12, initialTimeApp));

    // Print it
    print(appointmentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentAppointmentByIdFromSameParent() {
    User parent = buildMarcos(8);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int initialTimeApp = 10;
    int dayApp = 6;

    URI appointmentURI = postAppointment(initialTimeApp, dayApp, parentID, "carl@email.com");

    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
    String appointmentID = idAppPath.toString();

    Appointment appointmentResp =
        RestFactory.doGetRequest(parent, "parents", parentID, "appointments", appointmentID)
            .readEntity(Appointment.class);

    assertEquals("carl@email.com", appointmentResp.getTeacher().getEmail());
    assertEquals(appointmentResp.getParent().getEmail(), parent.getEmail());
    assertEquals(
        appointmentResp.getDatetimeStart(),
        LocalDateTime.of(2018, Month.JANUARY, dayApp, 12, initialTimeApp));

    // Print it
    print(appointmentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putParentAppointmentFromParent() {
    User parent = buildMarcos(9);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    URI appointmentURI = postAppointment(15, 13, parentID, "carl@email.com");

    Path fullAppPath = Paths.get("/", appointmentURI.getPath());
    Path idAppPath = fullAppPath.getParent().relativize(fullAppPath);
    String appointmentID = idAppPath.toString();

    int newInitialTime = 20;
    int newDay = 13;

    ParentAppointmentRequest putParentAppointmentRequest =
        buildAppointment(newInitialTime, newDay, "carl@email.com");

    RestFactory.doPutRequest(
        parent, putParentAppointmentRequest, "parents", parentID, "appointments", appointmentID);

    // Make a new GET to compare results
    Appointment newAppointmentResponse =
        RestFactory.doGetRequest(parent, appointmentURI).readEntity(Appointment.class);

    assertEquals(newAppointmentResponse.getParent().getEmail(), parent.getEmail());
    assertEquals(newAppointmentResponse.getTeacher().getEmail(), "carl@email.com");
    assertEquals(
        newAppointmentResponse.getDatetimeStart(),
        LocalDateTime.of(2018, Month.JANUARY, newDay, 12, newInitialTime));
    assertEquals(
        newAppointmentResponse.getDatetimeEnd(),
        LocalDateTime.of(2018, Month.JANUARY, newDay, 12, newInitialTime + 10));

    print(newAppointmentResponse);
  }

  private ParentAppointmentRequest buildAppointment(int initialTime, int day, String teacherEmail) {

    Optional<User> teacherOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, teacherEmail);
    assertTrue(teacherOpt.isPresent());

    ParentAppointmentRequest appointment = new ParentAppointmentRequest();
    appointment.setTeacherId(teacherOpt.get().getId());
    appointment.setDatetimeStart(LocalDateTime.of(2018, Month.JANUARY, day, 12, initialTime));
    appointment.setDatetimeEnd(LocalDateTime.of(2018, Month.JANUARY, day, 12, initialTime + 10));
    appointment.setStatus(Appointment.Status.REQUESTED);
    return appointment;
  }

  private URI postAppointment(int initialTime, int day, String parentID, String teacherEmail) {

    // We post an appointment between parent and teacher Carl
    ParentAppointmentRequest parentAppointmentRequest =
        buildAppointment(initialTime, day, teacherEmail);

    return RestFactory.doPostRequest(
        adminForAuth, parentAppointmentRequest, "parents", parentID, "appointments");
  }
}

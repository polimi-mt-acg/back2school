package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.*;
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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;
import static org.junit.Assert.*;

public class TeachersResourceAppointmentsTest {

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
  public void getTeachersFromAdmin() {
    List<URI> teachersURIs =
        RestFactory.doGetRequest(adminForAuth, "teachers")
            .readEntity(TeachersResponse.class)
            .getTeachers();

    print("GET /teachers");
    for (URI uri : teachersURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherAppointmentsFromAdmin() {
    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);

    TeacherAppointmentsResponse teacherAppointmentsResponse =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacher.getId(), "appointments")
            .readEntity(TeacherAppointmentsResponse.class);
    assertEquals(3, teacherAppointmentsResponse.getAppointments().size());

    // Print it
    print("GET /teachers/", teacher.getId(), "/appointments");
    print(teacherAppointmentsResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherAppointmentsFromSameTeacher() {
    // Get teacher from seeds
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);

    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);
    assertTrue(teacher.weakEquals(seedTeacher)); // got the same teacher seeds/db

    TeacherAppointmentsResponse teacherAppointmentsResponse =
        RestFactory.doGetRequest(seedTeacher, "teachers", teacher.getId(), "appointments")
            .readEntity(TeacherAppointmentsResponse.class);
    assertEquals(3, teacherAppointmentsResponse.getAppointments().size());

    // Print it
    print("GET /teachers/", teacher.getId(), "/appointments");
    print(teacherAppointmentsResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeacherAppointmentFromTeacher() {
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 0);
    assertNotNull(teacherToPost);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");

    TeacherAppointmentRequest teacherAppointmentRequest =
        buildAppointment(1, 4, "alice2@email.com");

    Path fullPath = Paths.get("/", teacherURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String teacherID = idPath.toString();

    URI uri =
        RestFactory.doPostRequest(
            teacherToPost, teacherAppointmentRequest, "teachers", teacherID, "appointments");
    print("Created: ", uri, " -- from teacher");

    // Now query /teachers/{teacherId}/appointments from teacher
    TeacherAppointmentsResponse teacherAppointments =
        RestFactory.doGetRequest(teacherToPost, "teachers", teacherID, "appointments")
            .readEntity(TeacherAppointmentsResponse.class);

    assertTrue(teacherAppointments.getAppointments().size() > 0);

    // Print it
    print("GET /teachers/", teacherID, "/appointments");
    print(teacherAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTwoTeacherAppointmentsInConflict() {
    // Get teacher from seeds
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 1);
    assertNotNull(seedTeacher);

    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl2@email.com")
            .get(0);
    assertNotNull(teacher);
    assertTrue(teacher.weakEquals(seedTeacher)); // got the same teacher seeds/db

    // We post the first appointment between teacher and parent Alice
    TeacherAppointmentRequest teacherAppointmentRequest1 =
        buildAppointment(30, 1, "alice@email.com");

    URI uri =
        RestFactory.doPostRequest(
            seedTeacher, teacherAppointmentRequest1, "teachers", teacher.getId(), "appointments");
    print("[1] Created: ", uri);

    // Tested conflict between same teacher and same parent
    TeacherAppointmentRequest teacherAppointmentRequest2 =
        buildAppointment(35, 1, "alice@email.com");

    Invocation request2 =
        RestFactory.getAuthenticatedInvocationBuilder(
                seedTeacher, "teachers", str(teacher.getId()), "appointments")
            .buildPost(Entity.json(teacherAppointmentRequest2));
    Response response2 = request2.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response2.getStatus());

    print("[2] Conflict at: ", response2.toString());

    // Tested conflict between same teacher and different parent
    TeacherAppointmentRequest teacherAppointmentRequest3 =
        buildAppointment(35, 1, "alice2@email.com");

    Invocation request3 =
        RestFactory.getAuthenticatedInvocationBuilder(
                seedTeacher, "teachers", str(teacher.getId()), "appointments")
            .buildPost(Entity.json(teacherAppointmentRequest3));
    Response response3 = request3.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response3.getStatus());

    print("[3] ", response3.toString());

    // Tested conflict between different teacher and same parent
    // Get teacher from seeds
    User seedTeacher2 = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher2);

    // Get teacher from database
    User teacher2 =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher2);
    assertTrue(teacher2.weakEquals(seedTeacher2)); // got the same teacher seeds/db

    TeacherAppointmentRequest teacherAppointmentRequest4 =
        buildAppointment(30, 1, "alice@email.com");

    Invocation request4 =
        RestFactory.getAuthenticatedInvocationBuilder(
                seedTeacher2, "teachers", str(teacher2.getId()), "appointments")
            .buildPost(Entity.json(teacherAppointmentRequest4));
    Response response4 = request4.invoke();
    assertEquals(Response.Status.CONFLICT.getStatusCode(), response4.getStatus());

    print("[4] ", response4.toString());

    // Now query /teachers/{teacherId}/appointments
    // Only the first appointment should be retrieved by the teacher
    TeacherAppointmentsResponse teacherAppointments =
        RestFactory.doGetRequest(seedTeacher, "teachers", str(teacher.getId()), "appointments")
            .readEntity(TeacherAppointmentsResponse.class);

    assertEquals(1, teacherAppointments.getAppointments().size());

    // Print it
    print(teacherAppointments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherAppointmentByIdFromAdministrator() {
    Appointment appointment =
        DatabaseHandler.getInstance().getListSelectFrom(Appointment.class).get(0);
    assertNotNull(appointment);

    User teacher = appointment.getTeacher();

    Appointment appointmentResp =
        RestFactory.doGetRequest(
                adminForAuth, "teachers", teacher.getId(), "appointments", appointment.getId())
            .readEntity(Appointment.class);

    assertEquals(appointment.getTeacher().getEmail(), appointmentResp.getTeacher().getEmail());
    assertEquals(appointment.getParent().getEmail(), appointmentResp.getParent().getEmail());
    assertEquals(appointment.getStatus(), appointmentResp.getStatus());
    assertEquals(appointment.getDatetimeStart(), appointmentResp.getDatetimeStart());

    // Print it
    print(appointmentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherAppointmentByIdFromSameTeacher() {
    Appointment appointment =
        DatabaseHandler.getInstance().getListSelectFrom(Appointment.class).get(0);
    assertNotNull(appointment);

    // get appointment teacher
    User teacher = appointment.getTeacher();

    // get the same teacher from seed for auth
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);
    assertTrue(teacher.weakEquals(seedTeacher)); // got the same teacher seeds/db

    Appointment appointmentResp =
        RestFactory.doGetRequest(
                seedTeacher, "teachers", teacher.getId(), "appointments", appointment.getId())
            .readEntity(Appointment.class);

    assertEquals(appointment.getTeacher().getEmail(), appointmentResp.getTeacher().getEmail());
    assertEquals(appointment.getParent().getEmail(), appointmentResp.getParent().getEmail());
    assertEquals(appointment.getStatus(), appointmentResp.getStatus());
    assertEquals(appointment.getDatetimeStart(), appointmentResp.getDatetimeStart());

    // Print it
    print(appointmentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putTeacherAppointmentFromTeacher() {
    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);

    // Get teacher from seeds
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);
    assertTrue(teacher.weakEquals(seedTeacher)); // got the same teacher seeds/db

    TeacherAppointmentRequest appointmentRequest = buildAppointment(15, 13, "alice@email.com");
    URI appointmentURI =
        RestFactory.doPostRequest(
            seedTeacher, appointmentRequest, "teachers", teacher.getId(), "appointments");

    Path fullPath = Paths.get("/", appointmentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String appointmentID = idPath.toString();

    int newInitialTime = 20;
    int newDay = 13;

    TeacherAppointmentRequest putTeacherAppointmentRequest =
        buildAppointment(newInitialTime, newDay, "alice@email.com");

    print("PUT /teachers/", teacher.getId(), "/appointments/", appointmentID);
    RestFactory.doPutRequest(
        seedTeacher,
        putTeacherAppointmentRequest,
        "teachers",
        teacher.getId(),
        "appointments",
        appointmentID);
    print("Done.");

    // Make a new GET to compare results
    Appointment newAppointmentResponse =
        RestFactory.doGetRequest(seedTeacher, appointmentURI).readEntity(Appointment.class);

    assertEquals(putTeacherAppointmentRequest.getStatus(), newAppointmentResponse.getStatus());
    assertEquals(
        putTeacherAppointmentRequest.getDatetimeStart(), newAppointmentResponse.getDatetimeStart());
    assertEquals(
        putTeacherAppointmentRequest.getDatetimeEnd(), newAppointmentResponse.getDatetimeEnd());

    print("GET ", appointmentURI);
    print(newAppointmentResponse);
  }

  private TeacherAppointmentRequest buildAppointment(int initialTime, int day, String parentEmail) {
    Optional<User> teacherOpt = DatabaseHandler.fetchEntityBy(User.class, User_.email, parentEmail);
    assertTrue(teacherOpt.isPresent());

    TeacherAppointmentRequest appointmentRequest = new TeacherAppointmentRequest();
    appointmentRequest.setParentId(teacherOpt.get().getId());
    appointmentRequest.setDatetimeStart(LocalDateTime.of(2018, Month.MAY, day, 12, initialTime));
    appointmentRequest.setDatetimeEnd(LocalDateTime.of(2018, Month.MAY, day, 12, initialTime + 15));
    appointmentRequest.setStatus(Appointment.Status.REQUESTED);
    return appointmentRequest;
  }
}

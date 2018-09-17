package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.*;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;
import static org.junit.Assert.*;

public class TeachersResourceTest {

  private static HttpServer server;
  private static User adminForLogin;

  @BeforeClass
  public static void oneTimeSetUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioTeachers");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.teachers",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
  }

  @AfterClass
  public static void oneTimeTearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Before
  public void setUp() {
    // Admin account for the log in
    adminForLogin = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.ADMINISTRATOR);
    assertNotNull(adminForLogin);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeachers() throws IOException {
    // Get seeds' users
    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioTeachers", "users.json");
    assertNotNull(seedUsers);

    // get the teachers from seeds
    List<User> seedTeachers =
        seedUsers
            .stream()
            .filter(user -> user.getRole().equals(User.Role.TEACHER))
            .collect(Collectors.toList());

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "teachers").buildGet();

    TeachersResponse response = request.invoke(TeachersResponse.class);
    assertNotNull(response);

    List<User> responseTeachers = response.getTeachers();

    // for each seed user check if it exist in the response
    for (User seedTeacher : seedTeachers) {
      // look for the the current seed user in the response
      User responseTeacher =
          responseTeachers
              .stream()
              .filter(user -> user.weakEquals(seedTeacher))
              .collect(Collectors.toList())
              .get(0);

      assertNotNull(responseTeacher);
      assertTrue(seedTeacher.weakEquals(responseTeacher));
    }

    print("Response of /teachers :\n");
    print(response);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeachers() {
    // Get a user to be inserted
    User carlosPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachers_ToPost1", User.Role.TEACHER);
    assertNotNull(carlosPost);

    URI insertedTeacherURI = doTeacherPost(carlosPost);

    print("Inserted teacher URI: ", insertedTeacherURI);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherByIdFromAdmin() {
    // Get a user to be inserted
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachers_ToPost2", User.Role.TEACHER);
    assertNotNull(seedTeacher);

    URI insertedTeacherURI = doTeacherPost(seedTeacher);
    Path fullPath = Paths.get("/", insertedTeacherURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String teacherID = idPath.toString();

    System.out.println("New inserted teacher id: " + teacherID);

    // GET - Using admin to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "teachers", teacherID)
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    User teacherFromResponse = response.readEntity(User.class);
    assertTrue(teacherFromResponse.weakEquals(seedTeacher));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherByIdFromTeacher() {
    // Get a user to be inserted and then use the same to log in
    User seedTeacher =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachers_ToPost3", User.Role.TEACHER);
    assertNotNull(seedTeacher);

    URI insertedTeacherURI = doTeacherPost(seedTeacher);
    print("seedTeacher.getSeedPassword: " + String.valueOf(seedTeacher.getNewPassword()));
    Path fullPath = Paths.get("/", insertedTeacherURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String teacherID = idPath.toString();

    print("New inserted teacher id: " + teacherID);

    // GET - Using teacher to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(seedTeacher, "teachers", teacherID)
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    User teacherFromResponse = response.readEntity(User.class);
    assertTrue(teacherFromResponse.weakEquals(seedTeacher));
  }

  /**
   * Do a post and return the inserted teacher URI.
   *
   * @return The inserted resource URI.
   */
  private URI doTeacherPost(User user) {
    // Make a POST to /teachers
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "teachers")
            .buildPost(Entity.json(user));

    Response response = post.invoke();
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesFromAdmin() {
    // Get the first teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);

    // GET - Using admin to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "teachers", String.valueOf(carl1Teacher.getId()), "classes")
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TeacherClassesResponse teacherClassesResponse =
        response.readEntity(TeacherClassesResponse.class);

    print("Response /teachers/", carl1Teacher.getId(), "/classes:\n", teacherClassesResponse);

    assertEquals(teacherClassesResponse.getClasses().size(), 2);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesFromTeacher() {
    // Get the first teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);
    assertNotNull(carl1Teacher);
    assertEquals(carl1Teacher.getEmail(), "carl1@email.com");
    // set the password in order to let the invocation builder be able to authenticate the user
    carl1Teacher.setNewPassword("email_password");

    // GET - Using teacher to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                carl1Teacher, "teachers", String.valueOf(carl1Teacher.getId()), "classes")
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TeacherClassesResponse teacherClassesResponse =
        response.readEntity(TeacherClassesResponse.class);

    print("Response /teachers/", carl1Teacher.getId(), "/classes:\n", teacherClassesResponse);

    assertEquals(teacherClassesResponse.getClasses().size(), 2);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesQueryYear() {
    // Get the first teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);

    // GET - Using admin to log in
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes
    String[] path = {"teachers", str(carl1Teacher.getId()), "classes"};
    for (String p : path) {
      target = target.path(p);
    }
    // add query param to select the year to filter on
    target = target.queryParam("year", "2017");

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, target).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TeacherClassesResponse teacherClassesResponse =
        response.readEntity(TeacherClassesResponse.class);

    print("Response ", target.getUri().toString(), "\n", teacherClassesResponse);
    assertEquals(teacherClassesResponse.getClasses().size(), 1);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromAdmin() {
    // Get teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);

    assertNotNull(carl1Teacher);

    // Get class from database
    Class class1B =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "1B")
            .get(0);

    // GET - Using admin to log in
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes/{id}/timetable
    String[] path = {
      "teachers", str(carl1Teacher.getId()), "classes", str(class1B.getId()), "timetable"
    };
    for (String p : path) {
      target = target.path(p);
    }

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, target).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TimetableResponse timetableResponse = response.readEntity(TimetableResponse.class);

    print("Response to ", target.getUri().toString(), " :\n", timetableResponse);
    // must be equal to 2 since both the years, not query param passed to filter
    // on the year
    assertEquals(timetableResponse.getLectures().size(), 2);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromTeacher() {
    // Get teacher from database
    User carl2Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl2@email.com")
            .get(0);
    assertNotNull(carl2Teacher);
    assertEquals(carl2Teacher.getEmail(), "carl2@email.com");
    // set the password in order to let the invocation builder be able to authenticate the user
    carl2Teacher.setNewPassword("email_password");

    // Get class from database
    Class class3B =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "3B")
            .get(0);

    // GET - Using teacher to log in
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes
    String[] path = {
      "teachers", str(carl2Teacher.getId()), "classes", str(class3B.getId()), "timetable"
    };
    for (String p : path) {
      target = target.path(p);
    }

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(carl2Teacher, target).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TimetableResponse timetableResponse = response.readEntity(TimetableResponse.class);

    print(
        "Invoked as " + carl2Teacher.getEmail() + ", id: " + str(carl2Teacher.getId()) + "\n",
        "Response to ",
        target.getUri().toString(),
        " :\n",
        timetableResponse);
    // must be equal to 2 since both the years, not query param passed to filter
    // on the year
    assertEquals(timetableResponse.getLectures().size(), 1);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromAdminQueryYear() {
    // Get teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);

    assertNotNull(carl1Teacher);

    // Get class from database
    Class class1B =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "1B")
            .get(0);

    // GET - Using admin to log in
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes/{id}/timetable?year=2017
    String[] path = {
      "teachers", str(carl1Teacher.getId()), "classes", str(class1B.getId()), "timetable"
    };
    for (String p : path) {
      target = target.path(p);
    }
    // add query param to select the year to filter on
    target = target.queryParam("year", "2017");

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, target).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TimetableResponse timetableResponse = response.readEntity(TimetableResponse.class);

    print("Response to ", target.getUri().toString(), " :\n", timetableResponse);
    // must be equal to 1 since query param is passed to filter on the year
    assertEquals(timetableResponse.getLectures().size(), 1);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherAppointments() {
    // Get teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(carl1Teacher);

    // GET - Using admin to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "teachers", str(carl1Teacher.getId()), "appointments")
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    TeacherAppointmentsResponse teacherAppointmentsResponse =
        response.readEntity(TeacherAppointmentsResponse.class);

    print(
        "Response to /teachers/",
        carl1Teacher.getId(),
        "/appointments :\n",
        teacherAppointmentsResponse);
    assertEquals(teacherAppointmentsResponse.getAppointments().size(), 1);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeacherAppointments() {
    Session session = DatabaseHandler.getInstance().getNewSession();

    // get teacher from db
    Optional<User> teacherOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "carl2@email.com", session);
    assertTrue(teacherOpt.isPresent());
    User teacher = teacherOpt.get();
    // set the password in order to let the invocation builder be able to authenticate the user
    teacher.setNewPassword("email_password");

    // get parent from db
    Optional<User> parentOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice2@email.com", session);
    assertTrue(parentOpt.isPresent());
    User parent = parentOpt.get();

    // create new appointment request
    AppointmentRequest appointmentRequest = new AppointmentRequest();
    appointmentRequest.setParentId(parent.getId());
    appointmentRequest.setDatetimeStart(LocalDateTime.parse("2018-06-14T12:15:00"));
    appointmentRequest.setDatetimeEnd(LocalDateTime.parse("2018-06-14T13:15:00"));
    appointmentRequest.setStatus(Appointment.Status.REQUESTED);

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                teacher, "teachers", str(teacher.getId()), "appointments")
            .buildPost(Entity.json(appointmentRequest));

    Response response = postRequest.invoke();
    assertNotNull(response);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI createdEntityURI = response.getLocation();

    Path fullPath = Paths.get("/", createdEntityURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String appointmentId = idPath.toString();
    print("URI: ", fullPath.toString());
    print("appointmentId: ", appointmentId);

    // Create Get request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                teacher, "teachers", str(teacher.getId()), "appointments", appointmentId)
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    TeacherAppointmentsResponse.Entity appointmentResponse =
        getResponse.readEntity(TeacherAppointmentsResponse.Entity.class);
    print("Response: \n", appointmentResponse);

    assertNotNull(appointmentResponse);
    assertEquals(
        appointmentRequest.getDatetimeStart().toString(), appointmentResponse.getDatetimeStart());
    assertEquals(
        appointmentRequest.getDatetimeEnd().toString(), appointmentResponse.getDatetimeEnd());
    assertEquals(appointmentRequest.getStatus().toString(), appointmentResponse.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putTeacherAppointment() {
    Session session = DatabaseHandler.getInstance().getNewSession();

    // get teacher from db
    Optional<User> teacherOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "carl2@email.com", session);
    assertTrue(teacherOpt.isPresent());
    User teacher = teacherOpt.get();
    // set the password in order to let the invocation builder be able to authenticate the user
    teacher.setNewPassword("email_password");

    // get parent from db
    Optional<User> parentOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice@email.com", session);
    assertTrue(parentOpt.isPresent());
    User parent = parentOpt.get();

    // create new appointment request
    AppointmentRequest appointmentRequest = new AppointmentRequest();
    appointmentRequest.setParentId(parent.getId());
    appointmentRequest.setDatetimeStart(LocalDateTime.parse("2018-03-10T16:15:00"));
    appointmentRequest.setDatetimeEnd(LocalDateTime.parse("2018-03-10T17:15:00"));
    appointmentRequest.setStatus(Appointment.Status.REQUESTED);

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                teacher, "teachers", str(teacher.getId()), "appointments")
            .buildPost(Entity.json(appointmentRequest));

    Response response = postRequest.invoke();
    assertNotNull(response);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI createdEntityURI = response.getLocation();

    Path fullPath = Paths.get("/", createdEntityURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String appointmentId = idPath.toString();
    print("URI: ", fullPath.toString());
    print("appointmentId: ", appointmentId);

    // Create PUT request entity to modify the latter saved appointment
    AppointmentRequest putAppointmentRequest = new AppointmentRequest();
    putAppointmentRequest.setParentId(parent.getId());
    putAppointmentRequest.setDatetimeStart(LocalDateTime.parse("2018-03-12T16:15:00"));
    putAppointmentRequest.setDatetimeEnd(LocalDateTime.parse("2018-03-12T17:15:00"));
    putAppointmentRequest.setStatus(Appointment.Status.COUNTERPROPOSED);

    // Create PUT request
    Invocation putRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                teacher, "teachers", str(teacher.getId()), "appointments", appointmentId)
            .buildPut(Entity.json(putAppointmentRequest));
    Response putResponse = putRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), putResponse.getStatus());

    // Create Get request to very entity to be updated
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                teacher, "teachers", str(teacher.getId()), "appointments", appointmentId)
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    TeacherAppointmentsResponse.Entity appointmentResponse =
        getResponse.readEntity(TeacherAppointmentsResponse.Entity.class);
    print("Response: \n", appointmentResponse);

    assertNotNull(appointmentResponse);
    assertNotEquals(
        appointmentRequest.getDatetimeStart().toString(), appointmentResponse.getDatetimeStart());
    assertNotEquals(
        appointmentRequest.getDatetimeEnd().toString(), appointmentResponse.getDatetimeEnd());
    assertNotEquals(appointmentRequest.getStatus().toString(), appointmentResponse.getStatus());

    assertEquals(
        putAppointmentRequest.getDatetimeStart().toString(),
        appointmentResponse.getDatetimeStart());
    assertEquals(
        putAppointmentRequest.getDatetimeEnd().toString(), appointmentResponse.getDatetimeEnd());
    assertEquals(putAppointmentRequest.getStatus().toString(), appointmentResponse.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotifications() {
    // Get teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);

    assertNotNull(carl1Teacher);
    carl1Teacher.setNewPassword("email_password");

    // Create Get request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                carl1Teacher, "teachers", str(carl1Teacher.getId()), "notifications")
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());
    TeacherNotificationsResponse teacherNotificationsResponse =
        getResponse.readEntity(TeacherNotificationsResponse.class);

    print(
        "Response to /teachers/",
        carl1Teacher.getId(),
        "/notifications :\n",
        teacherNotificationsResponse);
    assertEquals(4, teacherNotificationsResponse.getNotifications().size());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherNotificationById() {
    // Get teacher from database
    User carl1Teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);

    assertNotNull(carl1Teacher);
    carl1Teacher.setNewPassword("email_password");

    // Create Get request
    // NO FILTER, so all notifications: READ and UNREAD
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                carl1Teacher, "teachers", str(carl1Teacher.getId()), "notifications")
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());
    TeacherNotificationsResponse teacherNotificationsResponse =
        getResponse.readEntity(TeacherNotificationsResponse.class);

    print(
        "Response to /teachers/",
        carl1Teacher.getId(),
        "/notifications :\n",
        teacherNotificationsResponse);
    assertEquals(4, teacherNotificationsResponse.getNotifications().size());

    TeacherNotificationsResponse.Entity notificationToRead =
        teacherNotificationsResponse.getNotifications().get(0);

    // READ NOTIFICATION
    // Create Get request to read notification
    Invocation getRequest2 =
        RestFactory.getAuthenticatedInvocationBuilder(carl1Teacher, notificationToRead.getUrl())
            .buildGet();

    Response getResponse2 = getRequest2.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse2.getStatus());
    Notification notification = getResponse2.readEntity(Notification.class);
    print("Response to ", notificationToRead.getUrl().toString(), " :\n", notification);
    assertEquals(notificationToRead.getSubject(), notification.getSubject());

    // ASSERT notification of before is now READ
    Invocation getRequest3 =
        RestFactory.getAuthenticatedInvocationBuilder(
                carl1Teacher, "teachers", str(carl1Teacher.getId()), "notifications")
            .buildGet();

    Response getResponse3 = getRequest3.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse3.getStatus());

    TeacherNotificationsResponse teacherNotificationsResponse1 =
        getResponse3.readEntity(TeacherNotificationsResponse.class);
    print(
        "Response to teachers/",
        str(carl1Teacher.getId()),
        "/notifications :\n",
        teacherNotificationsResponse1);

    TeacherNotificationsResponse.Entity readNotification =
        teacherNotificationsResponse1
            .getNotifications()
            .stream()
            .filter(x -> x.getSubject().equals(notificationToRead.getSubject()))
            .collect(Collectors.toList())
            .get(0);

    assertEquals(Notification.Status.READ, readNotification.getStatus());
  }
}

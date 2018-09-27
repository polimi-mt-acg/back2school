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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;
import static org.junit.Assert.*;

public class TeachersResourceTest {

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
    List<URI> parentsURIs =
        RestFactory.doGetRequest(adminForAuth, "teachers")
            .readEntity(TeachersResponse.class)
            .getTeachers();

    print("GET /teachers");
    for (URI uri : parentsURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeachersFromParent() {
    // Get a teacher
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "teachers").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeachers() {
    // Get a user to be inserted
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 0);
    assertNotNull(teacherToPost);

    URI insertedURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");

    print("POST /teachers");
    print("Inserted entity URI: ", insertedURI);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherByIdFromAdmin() {
    // Get a user to be inserted
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 1);
    assertNotNull(teacherToPost);

    URI insertedURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");
    print("POST /teachers");
    print("Inserted entity URI: ", insertedURI);

    User teacherResponse =
        RestFactory.doGetRequest(adminForAuth, insertedURI).readEntity(User.class);

    assertTrue(teacherResponse.weakEquals(teacherToPost));

    print("GET ", insertedURI.toString(), " -- from same teacher");
    print("Response: ");
    print(teacherResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherByIdFromSameTeacher() {
    // Get a user to be inserted and then use the same to log in
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 2);
    assertNotNull(teacherToPost);

    URI insertedURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");
    print("POST /teachers");
    print("Inserted entity URI: ", insertedURI);

    User teacherResponse =
        RestFactory.doGetRequest(teacherToPost, insertedURI).readEntity(User.class);

    assertTrue(teacherResponse.weakEquals(teacherToPost));

    print("GET ", insertedURI.toString(), " -- from same teacher");
    print("Response: ");
    print(teacherResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherByIdFromNotSameTeacher() {
    // get a teacher from the scenario already deployed
    User teacherForAuth =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(teacherForAuth);

    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 3);
    assertNotNull(teacherToPost);

    URI insertedURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");
    print("POST /teachers");
    print("Inserted entity URI: ", insertedURI);

    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(teacherForAuth, insertedURI)
            .buildGet()
            .invoke();

    assertEquals(Status.FORBIDDEN.getStatusCode(), getResponse.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putTeacherByIdFromAdmin() {
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 4);
    assertNotNull(teacherToPost);
    User teacherToPut =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 5);
    assertNotNull(teacherToPut);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");

    User teacherGetResponse =
        RestFactory.doGetRequest(teacherToPost, teacherURI).readEntity(User.class);
    print("GET ", teacherURI.toString());
    print(teacherGetResponse);

    // Make a PUT request
    RestFactory.doPutRequest(adminForAuth, teacherToPut, teacherURI);
    print("PUT ", teacherURI.toString());
    print("Done.");

    // Make a new GET to compare results
    User newTeacherResponse =
        RestFactory.doGetRequest(adminForAuth, teacherURI).readEntity(User.class);

    assertTrue(newTeacherResponse.weakEquals(teacherToPut));

    print("GET ", teacherURI.toString());
    print("Response: ");
    print(teacherGetResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putTeacherByIdFromSameTeacher() {
    User teacherToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 6);
    assertNotNull(teacherToPost);
    User teacherToPut =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachersToPost", User.Role.TEACHER, 7);
    assertNotNull(teacherToPut);

    URI teacherURI = RestFactory.doPostRequest(adminForAuth, teacherToPost, "teachers");

    User teacherGetResponse =
        RestFactory.doGetRequest(teacherToPost, teacherURI).readEntity(User.class);
    print("GET ", teacherURI.toString());
    print(teacherGetResponse);

    // Make a PUT request
    RestFactory.doPutRequest(teacherToPost, teacherToPut, teacherURI);
    print("PUT ", teacherURI.toString());
    print("Done.");

    // Make a new GET to compare results
    User newTeacherResponse =
        RestFactory.doGetRequest(teacherToPut, teacherURI).readEntity(User.class);

    assertTrue(newTeacherResponse.weakEquals(teacherToPut));

    print("GET ", teacherURI.toString());
    print("Response: ");
    print(teacherGetResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesFromAdmin() {
    // Get the first teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);

    TeacherClassesResponse teacherClassesResponse =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacher.getId(), "classes")
            .readEntity(TeacherClassesResponse.class);

    assertEquals(teacherClassesResponse.getClasses().size(), 2);

    print("GET /teachers/", teacher.getId(), "/classes");
    print(teacherClassesResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesFromTeacher() {
    // Get the first teacher from seeds
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);

    // Get the first teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);
    assertNotNull(teacher);
    assertTrue(teacher.weakEquals(seedTeacher)); // got the same teacher seeds/db

    TeacherClassesResponse teacherClassesResponse =
        RestFactory.doGetRequest(adminForAuth, "teachers", teacher.getId(), "classes")
            .readEntity(TeacherClassesResponse.class);

    assertEquals(teacherClassesResponse.getClasses().size(), 2);

    print("GET /teachers/", teacher.getId(), "/classes");
    print(teacherClassesResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherClassesQueryYear() {
    // Get the first teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER)
            .get(0);

    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes
    target = target.path("teachers").path(str(teacher.getId())).path("classes");

    // add query param to select the year to filter on
    target = target.queryParam("year", "2017");

    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, target).buildGet().invoke();

    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    TeacherClassesResponse teacherClassesResponse =
        getResponse.readEntity(TeacherClassesResponse.class);
    assertEquals(1, teacherClassesResponse.getClasses().size());

    print("GET ", target.getUri().toString());
    print(teacherClassesResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromAdmin() {
    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);

    // Get class from database
    Class aClass =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "1B")
            .get(0);
    assertNotNull(aClass);

    TimetableResponse timetableResponse =
        RestFactory.doGetRequest(
                adminForAuth, "teachers", teacher.getId(), "classes", aClass.getId(), "timetable")
            .readEntity(TimetableResponse.class);

    assertEquals(1, timetableResponse.getLectures().size());

    print(
        "GET ",
        "/teachers/",
        teacher.getId(),
        "/classes/",
        aClass.getId(),
        "/timetable -- from admin");
    print(timetableResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromTeacher() {
    // Get teacher from seeds
    User seedTeacher = DatabaseSeeder.getSeedUserByRole("scenarioTeachers", User.Role.TEACHER, 0);
    assertNotNull(seedTeacher);

    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);
    assertTrue(seedTeacher.weakEquals(teacher)); // got the same teacher seeds/db

    // Get class from database
    Class aClass =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "1B")
            .get(0);
    assertNotNull(aClass);

    TimetableResponse timetableResponse =
        RestFactory.doGetRequest(
                seedTeacher, "teachers", teacher.getId(), "classes", aClass.getId(), "timetable")
            .readEntity(TimetableResponse.class);

    assertEquals(1, timetableResponse.getLectures().size());

    print(
        "GET ",
        "/teachers/",
        teacher.getId(),
        "/classes/",
        aClass.getId(),
        "/timetable -- from teacher");
    print(timetableResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getTeacherTimetableFromAdminQueryYear() {
    // Get teacher from database
    User teacher =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.email, "carl1@email.com")
            .get(0);
    assertNotNull(teacher);

    // Get class from database
    Class aClass =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Class.class, Class_.name, "1B")
            .get(0);
    assertNotNull(aClass);

    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set path as /teachers/{id}/classes
    target =
        target
            .path("teachers")
            .path(str(teacher.getId()))
            .path("classes")
            .path(str(aClass.getId()))
            .path("timetable");

    // add query param to select the year to filter on
    target = target.queryParam("year", "2017");

    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, target).buildGet().invoke();

    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    TimetableResponse teacherTimetableResponse = getResponse.readEntity(TimetableResponse.class);

    // expected 0 if year=2017, if no year (or year=2018) would be 1
    assertEquals(0, teacherTimetableResponse.getLectures().size());

    print("GET ", target.getUri().toString(), " -- from admin");
    print(teacherTimetableResponse);
  }
}

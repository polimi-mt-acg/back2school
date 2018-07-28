package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.PostTeacherRequest;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.TeacherClassesResponse;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.TeacherResponse;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import org.glassfish.grizzly.http.server.HttpServer;
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
import java.util.List;
import java.util.stream.Collectors;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;
import static org.junit.Assert.*;

public class TeacherResourceTest {

  private static HttpServer server;
  private static User adminForLogin;

  @BeforeClass
  public static void oneTimeSetUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioTeachers");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class, "com.github.polimi_mt_acg.back2school.api.v1.teachers");
  }

  @AfterClass
  public static void oneTimeTearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

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

    TeacherResponse response = request.invoke(TeacherResponse.class);
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

    // Print it
    System.out.println(
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postTeachers() {
    // Get a user to be inserted
    User carlosPost =
        DatabaseSeeder.getSeedUserByRole("scenarioTeachers_ToPost1", User.Role.TEACHER);
    assertNotNull(carlosPost);

    URI insertedTeacherURI = doTeacherPost(carlosPost);

    System.out.println("Inserted teacher URI:");
    System.out.println(insertedTeacherURI);
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
    System.out.println(
        "seedTeacher.getSeedPassword: " + String.valueOf(seedTeacher.getSeedPassword()));
    Path fullPath = Paths.get("/", insertedTeacherURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String teacherID = idPath.toString();

    System.out.println("New inserted teacher id: " + teacherID);

    // GET - Using teacher to log in
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(seedTeacher, "teachers", teacherID)
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    User teacherFromResponse = response.readEntity(User.class);
    assertTrue(teacherFromResponse.weakEquals(seedTeacher));
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
    carl1Teacher.setSeedPassword("email_password");

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

  /**
   * Do a post an return the inserted teacher URI.
   *
   * @return The inserted resource URI.
   */
  private URI doTeacherPost(User user) {
    // Now build a PostStudentRequest
    PostTeacherRequest request = new PostTeacherRequest();
    request.setTeacherAndPassword(user, user.getSeedPassword());

    // Make a POST to /teachers
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "teachers")
            .buildPost(Entity.json(request));

    Response response = post.invoke();

    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

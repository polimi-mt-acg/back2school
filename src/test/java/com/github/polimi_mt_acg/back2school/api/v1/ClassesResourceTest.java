package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.classes.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Class_;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;
import static org.junit.Assert.*;

public class ClassesResourceTest {

  private static HttpServer server;
  private static User adminForLogin;

  @BeforeClass
  public static void oneTimeSetUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioClasses");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.classes",
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
    adminForLogin = DatabaseSeeder.getSeedUserByRole("scenarioClasses", User.Role.ADMINISTRATOR);
    assertNotNull(adminForLogin);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClasses() {
    List<Class> seedClasses =
        (List<Class>) DatabaseSeeder.getEntitiesListFromSeed("scenarioClasses", "classes.json");
    assertNotNull(seedClasses);

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes").buildGet();

    ClassesResponse response = request.invoke(ClassesResponse.class);
    assertNotNull(response);
    print("Response of GET at /classes :\n");
    print(response);

    // for each seed class check if it exist in the response
    List<ClassesResponse.Entity> responseClasses = response.getClasses();
    for (Class cls : seedClasses) {

      // look for the the current seed class in the response
      ClassesResponse.Entity classFromResponse =
          responseClasses
              .stream()
              .filter(x -> x.getName().equals(cls.getName()))
              .collect(Collectors.toList())
              .get(0);

      assertNotNull(classFromResponse);
      assertEquals(cls.getName(), classFromResponse.getName());
      assertEquals(cls.getAcademicYear(), classFromResponse.getAcademicYear());
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClasses() {
    // get classes to post
    List<Class> seedClasses =
        (List<Class>)
            DatabaseSeeder.getEntitiesListFromSeed("scenarioClasses_ToPost1", "classes.json");
    assertNotNull(seedClasses);

    Class seedClass =
        seedClasses
            .stream()
            .filter(x -> x.getName().equals("3A"))
            .collect(Collectors.toList())
            .get(0);
    assertNotNull(seedClass);

    // populate seedClass with students entities
    seedClass.prepareToPersist();
    // now seedClass contains instantiated students entities

    // create new class request
    ClassRequest classRequest = new ClassRequest();
    classRequest.setName(seedClass.getName());
    classRequest.setAcademicYear(seedClass.getAcademicYear());
    // set students ids from the seedClass list of students
    classRequest.setStudentsIds(
        seedClass.getClassStudents().stream().map(User::getId).collect(Collectors.toList()));

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes")
            .buildPost(Entity.json(classRequest));

    Response response = postRequest.invoke();
    assertNotNull(response);
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI createdEntityURI = response.getLocation();
    assertNotNull(createdEntityURI);

    // verify inserted entity
    Path fullPath = Paths.get("/", createdEntityURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String classId = idPath.toString();
    print("URI: ", fullPath.toString());
    print("classId: ", classId);

    // Create Get request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes", classId).buildGet();

    Response getResponse = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    ClassResponse classResponse = getResponse.readEntity(ClassResponse.class);
    print("Response to GET at /classes/", classId, ": \n", classResponse);

    assertNotNull(classResponse);
    assertEquals(classRequest.getName(), classResponse.getName());
    assertEquals(classRequest.getAcademicYear(), Integer.valueOf(classResponse.getAcademicYear()));

    // restore database status
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseSeeder.deployScenario("scenarioClasses");
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassesBadStudentId() {
    // create new class request
    ClassRequest classRequest = new ClassRequest();
    classRequest.setName("any");
    classRequest.setAcademicYear(3000);
    // add a student id which won't be present in the database
    classRequest.getStudentsIds().add(4012);

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes")
            .buildPost(Entity.json(classRequest));

    Response response = postRequest.invoke();
    assertNotNull(response);
    assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassById() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "1A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    // Create GET request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes", str(aClass.getId()))
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertNotNull(getResponse);
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    ClassResponse classResponse = getResponse.readEntity(ClassResponse.class);
    print("Response to GET at /classes/", aClass.getId(), ": \n", classResponse);

    assertNotNull(classResponse);
    assertEquals(aClass.getName(), classResponse.getName());
    assertEquals(aClass.getAcademicYear(), classResponse.getAcademicYear());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putClassById() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "1A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    assertEquals(2, aClass.getClassStudents().size());
    print("Original class entity with id: ", aClass.getId(), "\n", aClass);

    // create new class request
    ClassRequest classRequest = new ClassRequest();
    // let's change the name
    classRequest.setName("8A");
    // the year
    classRequest.setAcademicYear(aClass.getAcademicYear() + 1);
    // and remove the last student keeping only the first one.
    classRequest.setStudentsIds(new ArrayList<>());
    classRequest.getStudentsIds().add(aClass.getClassStudents().get(0).getId());

    // Create PUT request
    Invocation putRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes", str(aClass.getId()))
            .buildPut(Entity.json(classRequest));
    Response putResponse = putRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), putResponse.getStatus());

    // Verify that changes have been applied
    // Create GET request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes", str(aClass.getId()))
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertNotNull(getResponse);
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    ClassResponse classResponse = getResponse.readEntity(ClassResponse.class);
    print("Response to GET at /classes/", aClass.getId(), ": \n", classResponse);

    assertNotNull(classResponse);
    assertEquals(classRequest.getName(), classResponse.getName());
    assertEquals(classRequest.getAcademicYear(), Integer.valueOf(classResponse.getAcademicYear()));
    assertEquals(classRequest.getStudentsIds().size(), classResponse.getStudents().size());

    // restore database status
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseSeeder.deployScenario("scenarioClasses");
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putClassByIdBadStudentId() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    // create new class request
    ClassRequest classRequest = new ClassRequest();
    // let's change the name
    classRequest.setName("10A");
    // the year
    classRequest.setAcademicYear(aClass.getAcademicYear() + 1);
    classRequest.setStudentsIds(new ArrayList<>());
    // random id which is not (hopefully) on the database
    classRequest.getStudentsIds().add(1009);

    // Create PUT request
    Invocation putRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForLogin, "classes", str(aClass.getId()))
            .buildPut(Entity.json(classRequest));
    Response putResponse = putRequest.invoke();
    assertEquals(Status.BAD_REQUEST.getStatusCode(), putResponse.getStatus());

    // restore database status
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseSeeder.deployScenario("scenarioClasses");
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassStudents() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "1A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    assertEquals(2, aClass.getClassStudents().size());

    // Create GET request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "classes", str(aClass.getId()), "students")
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertNotNull(getResponse);
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    ClassStudentsResponse classStudentsResponse =
        getResponse.readEntity(ClassStudentsResponse.class);
    print("Response to GET at /classes/", aClass.getId(), "/students: \n", classStudentsResponse);

    assertNotNull(classStudentsResponse);
    assertEquals(aClass.getClassStudents().size(), classStudentsResponse.getStudents().size());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassStudents() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    assertEquals(2, aClass.getClassStudents().size());

    // fetch alice1
    Optional<User> alice1Opt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice1@email.com");
    assertTrue(alice1Opt.isPresent());
    User alice1 = alice1Opt.get();

    // create new class request
    ClassStudentsRequest classStudentsRequest = new ClassStudentsRequest();
    // add the student id to be added to the class
    classStudentsRequest.setStudentId(alice1.getId());

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "classes", str(aClass.getId()), "students")
            .buildPost(Entity.json(classStudentsRequest));

    Response putResponse = postRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), putResponse.getStatus());

    // Verify changes have been applied
    // Create GET request
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "classes", str(aClass.getId()), "students")
            .buildGet();

    Response getResponse = getRequest.invoke();
    assertNotNull(getResponse);
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    ClassStudentsResponse classStudentsResponse =
        getResponse.readEntity(ClassStudentsResponse.class);
    print("Response to GET at /classes/", aClass.getId(), "/students: \n", classStudentsResponse);

    assertNotNull(classStudentsResponse);
    assertEquals(aClass.getClassStudents().size() + 1, classStudentsResponse.getStudents().size());

    // restore database status
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseSeeder.deployScenario("scenarioClasses");
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassStudentsBadStudentId() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    // create new class request
    ClassStudentsRequest classStudentsRequest = new ClassStudentsRequest();
    // random id which is not (hopefully) on the database
    classStudentsRequest.setStudentId(90);

    // Create POST request
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin, "classes", str(aClass.getId()), "students")
            .buildPost(Entity.json(classStudentsRequest));

    Response putResponse = postRequest.invoke();
    assertEquals(Status.BAD_REQUEST.getStatusCode(), putResponse.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void deleteClassStudentById() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    assertEquals(2, aClass.getClassStudents().size());

    // take the last student of the class to be removed
    User studentToRemove = aClass.getClassStudents().get(1);

    // Create DELETE request
    Invocation deleteRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForLogin,
                "classes",
                str(aClass.getId()),
                "students",
                str(studentToRemove.getId()))
            .buildDelete();

    Response deleteResponse = deleteRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), deleteResponse.getStatus());

    // Verify the class has a student less
    Optional<Class> classStudRemovedOpt =
        DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classStudRemovedOpt.isPresent());
    Class classStudRemoved = classStudRemovedOpt.get();
    assertEquals(aClass.getClassStudents().size() - 1, classStudRemoved.getClassStudents().size());

    // restore database status
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseSeeder.deployScenario("scenarioClasses");
  }


  @Test
  @Category(TestCategory.Endpoint.class)
  public void deleteClassStudentByIdBadStudentId() {
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.name, "2A");
    assertTrue(classOpt.isPresent());
    Class aClass = classOpt.get();

    assertEquals(2, aClass.getClassStudents().size());

    Integer wrongStudentId = 9000;

    // Create DELETE request
    Invocation deleteRequest =
        RestFactory.getAuthenticatedInvocationBuilder(
            adminForLogin,
            "classes",
            str(aClass.getId()),
            "students",
            str(wrongStudentId))
            .buildDelete();

    Response deleteResponse = deleteRequest.invoke();
    assertEquals(Status.BAD_REQUEST.getStatusCode(), deleteResponse.getStatus());
  }
}

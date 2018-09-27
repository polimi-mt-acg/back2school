package com.github.polimi_mt_acg.back2school.api.v1;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResponse;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ClassroomsResourceTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void oneTimeSetUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioClassrooms");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.classrooms",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioClassrooms", User.Role.ADMINISTRATOR);
    assertNotNull(adminForAuth);
  }

  @AfterClass
  public static void oneTimeTearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassroomsFromAdmin() {
    List<URI> classroomsURIs =
        RestFactory.doGetRequest(adminForAuth, "classrooms")
            .readEntity(ClassroomsResponse.class)
            .getClassrooms();

    print("GET /classrooms");
    for (URI uri : classroomsURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassroomsFromTeacher() {
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioClassrooms", Role.TEACHER);

    // GET from teacher
    Response response =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "classrooms").buildGet().invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassroomsFromAdmin() {
    Classroom classroom = buildClassroom(0);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassroomsFromTeacher() {
    Classroom classroom = buildClassroom(1);
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioClassrooms", Role.TEACHER);

    Response response =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "classrooms")
            .buildPost(Entity.json(classroom))
            .invoke();

    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postClassroomsFromAdminGetConflict() {
    Classroom classroom = buildClassroom(2);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());

    // insert it again so to get the conflict
    Response response =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "classrooms")
            .buildPost(Entity.json(classroom))
            .invoke();
    assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassroomByIdFromAdministrator() {
    Classroom classroom = buildClassroom(3);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());

    Path fullPath = Paths.get("/", resourceURI.getPath());
    String classroomId = fullPath.getParent().relativize(fullPath).toString();

    Classroom respClassroom =
        RestFactory.doGetRequest(adminForAuth, "classrooms", classroomId)
            .readEntity(Classroom.class);

    assertEquals(classroom.getName(), respClassroom.getName());
    assertEquals(classroom.getFloor(), respClassroom.getFloor());
    assertEquals(classroom.getBuilding(), respClassroom.getBuilding());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getClassroomByIdFromTeacher() {
    Classroom classroom = buildClassroom(4);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());

    Path fullPath = Paths.get("/", resourceURI.getPath());
    String classroomId = fullPath.getParent().relativize(fullPath).toString();

    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioClassrooms", Role.TEACHER);

    Classroom respClassroom =
        RestFactory.doGetRequest(teacher, "classrooms", classroomId).readEntity(Classroom.class);

    assertEquals(classroom.getName(), respClassroom.getName());
    assertEquals(classroom.getFloor(), respClassroom.getFloor());
    assertEquals(classroom.getBuilding(), respClassroom.getBuilding());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putClassroomByIdFromAdmin() {
    Classroom classroom = buildClassroom(5);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());

    Path fullPath = Paths.get("/", resourceURI.getPath());
    String classroomId = fullPath.getParent().relativize(fullPath).toString();

    String suffix = "sffx";
    int floorSuffix = 100;

    // let's change the entity a bit
    classroom.setName(classroom.getName() + suffix);
    classroom.setFloor(classroom.getFloor() + floorSuffix);
    classroom.setBuilding(classroom.getBuilding() + suffix);

    // Do the PUT request
    print("PUT /classrooms/", classroomId);
    RestFactory.doPutRequest(adminForAuth, classroom, "classrooms", classroomId);
    print("Done.");

    // Make a new GET to compare results
    Classroom classroomResp =
        RestFactory.doGetRequest(adminForAuth, "classrooms", classroomId)
            .readEntity(Classroom.class);

    assertEquals(classroom.getName(), classroomResp.getName());
    assertEquals(classroom.getFloor(), classroomResp.getFloor());
    assertEquals(classroom.getBuilding(), classroomResp.getBuilding());

    print("GET /classrooms/", classroomId);
    print(classroomResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putClassroomByIdFromTeacher() {
    Classroom classroom = buildClassroom(6);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, classroom, "classrooms");

    print("POST /classrooms");
    print(resourceURI.toString());

    Path fullPath = Paths.get("/", resourceURI.getPath());
    String classroomId = fullPath.getParent().relativize(fullPath).toString();

    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioClassrooms", Role.TEACHER);

    Response response =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "classrooms", classroomId)
            .buildPut(Entity.json(classroom))
            .invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  private Classroom buildClassroom(int copyNumber) {
    Classroom classroom = new Classroom();
    classroom.setName("Classroom " + copyNumber);
    classroom.setFloor(copyNumber);
    classroom.setBuilding("Classroom building " + copyNumber);
    return classroom;
  }
}

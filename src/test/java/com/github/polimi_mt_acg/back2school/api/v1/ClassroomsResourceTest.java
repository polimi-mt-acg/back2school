package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.PutClassroomRequest;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
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

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioClassrooms");

    // Run HTTP server
    server =
            HTTPServerManager.startServer(
                    AuthenticationResource.class,
                    "com.github.polimi_mt_acg.back2school.api.v1.classrooms",
                    "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void getClassrooms() {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    Invocation getRequest =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "classrooms").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

//    System.out.println(admin);

    List<URI> classroomsURIs = response.readEntity(ClassroomsResponse.class).getClassrooms();

    for (URI uri : classroomsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void postClassrooms() {

    URI resourceURI = postB11(0);

    System.out.println(resourceURI);
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void getClassroomID() throws JsonProcessingException {
    // Create a new Classroom in the system
    URI B11URI = postB11(1);
    System.out.println(B11URI);

    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    // B11 ID
    Path fullPath = Paths.get("/", B11URI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String B11ID = idPath.toString();

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "classrooms", B11ID).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    Classroom B11Response = response.readEntity(Classroom.class);

    assertTrue(weakEquals(B11Response, buildB11(1)));

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(B11Response));
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void putClassroomById() throws JsonProcessingException {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    URI classroomURI = postB11(2);

    Invocation getClassroom =
            RestFactory.getAuthenticatedInvocationBuilder(admin, classroomURI).buildGet();
    Classroom classroomResponse = getClassroom.invoke().readEntity(Classroom.class);

    String nameSuffix = "newName";
    String buildingSuffix = "newBuilding";
    int floorSuffix = 100;

    // Create a PutStudent request to change its name
    PutClassroomRequest putClassroomRequest = new PutClassroomRequest();
    putClassroomRequest.setName(classroomResponse.getName() + nameSuffix);
    putClassroomRequest.setBuilding(classroomResponse.getBuilding() + buildingSuffix);
    putClassroomRequest.setFloor(classroomResponse.getFloor() + floorSuffix);

    // Make a PUT request
    Invocation putClassroom =
            RestFactory.getAuthenticatedInvocationBuilder(admin, classroomURI)
                    .buildPut(Entity.json(putClassroomRequest));
    Response putClassroomResponse = putClassroom.invoke();

    assertEquals(Status.OK.getStatusCode(), putClassroomResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetClassroom =
            RestFactory.getAuthenticatedInvocationBuilder(admin, classroomURI).buildGet();
    Classroom newClassroomResponse = newGetClassroom.invoke().readEntity(Classroom.class);

    assertEquals(classroomResponse.getName() + nameSuffix, newClassroomResponse.getName());
    assertEquals(classroomResponse.getBuilding() + buildingSuffix, newClassroomResponse.getBuilding());
    assertEquals(classroomResponse.getFloor() + floorSuffix, newClassroomResponse.getFloor());

    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newClassroomResponse));
  }


  private static HttpServer startServer() {
    // Create a resource config that scans for JAX-RS resources and providers
    // in com.github.polimi_mt_acg.back2school.api.v1.subjects.resources package
    final ResourceConfig rc =
            new ResourceConfig()
                    .register(AuthenticationResource.class)
                    .packages("com.github.polimi_mt_acg.back2school.api.v1.classrooms")
                    .register(JacksonCustomMapper.class)
                    .register(JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
  }

  private Classroom makeClassroom() {
    Classroom cr = new Classroom();
    cr.setName("A.1.1");
    cr.setBuilding("BL1");
    cr.setFloor(2);
    return cr;
  }

  private User get(Role role) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioClassrooms", "users.json");

    List<User> usersWithRole =
            users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private URI postB11(int copyNumber) {
    // Create a new Classroom in the system
    Classroom B11 = buildB11(copyNumber);

    // Get an admin to register B11 in the system
    User admin = get(Role.ADMINISTRATOR);


    // Make a POST to /students
    Invocation post =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "classrooms")
                    .buildPost(Entity.json(B11));

    Response response = post.invoke();
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  private Classroom buildB11(int copyNumber) {
    Classroom B11 = new Classroom();
    B11.setName("B11. " + copyNumber);
    B11.setBuilding("B" +copyNumber);
    B11.setFloor(11+copyNumber);
    B11.prepareToPersist();
    return B11;
  }

  private boolean weakEquals(Classroom u, Classroom p) {
    return u.getName().equals(p.getName())
            && u.getFloor() ==(p.getFloor())
            && u.getBuilding().equals(p.getBuilding());
  }
}

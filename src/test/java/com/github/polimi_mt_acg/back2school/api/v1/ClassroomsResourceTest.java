package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResponse;
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
//  @Category(TestCategory.Transient.class)
  public void postClassrooms() throws JsonProcessingException {
    // Get an admin
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioClassrooms", "users.json");

    admins =
        admins
            .stream()
            .filter(user -> user.getRole() == Role.ADMINISTRATOR)
            .collect(Collectors.toList());
    User admin = admins.get(0);

    // Create a custom classroom
    Classroom classroom = makeClassroom();

    // Authenticate the admin
    WebTarget target = RestFactory.buildWebTarget();
    // Authenticate
    String token = RestFactory.doLoginGetToken(admin.getEmail(), admin.getSeedPassword());
    assertNotNull(token);
    assertTrue(!token.isEmpty());

    // Set target to /notifications/send-to-teachers
    target = target.path("classrooms");

    // Set token and build the POST request
    Invocation request =
        target.request().header(HttpHeaders.AUTHORIZATION, token).buildPost(Entity.json(classroom));

    // Invoke the request
    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    Classroom responseSubj = response.readEntity(Classroom.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseSubj));

    assertNotNull(responseSubj);
    assertEquals(responseSubj.getName(), classroom.getName());
    assertEquals(responseSubj.getBuilding(), classroom.getBuilding());
    assertEquals(responseSubj.getFloor(), classroom.getFloor());
  }

  @Test
  public void getClassroomID() throws JsonProcessingException {
    // Get an admin
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioClassrooms", "users.json");

    admins =
        admins
            .stream()
            .filter(user -> user.getRole() == Role.ADMINISTRATOR)
            .collect(Collectors.toList());
    User admin = admins.get(0);

    // Authenticate the admin
    WebTarget target = RestFactory.buildWebTarget();
    // Authenticate
    String token = RestFactory.doLoginGetToken(admin.getEmail(), admin.getSeedPassword());
    assertNotNull(token);
    assertTrue(!token.isEmpty());

    // Set target to /subjects/1
    target = target.path("classrooms").path("1");

    // Set token and build the GET request
    Invocation request =
        target
            .request(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, token)
            .buildGet();

    // Invoke the request
    ClassroomsResponse response = request.invoke(ClassroomsResponse.class);
    assertNotNull(response);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
        "----CLASSROOMS / ID ----"
            + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
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
}

package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
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

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdministratorsResourceTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioAdministrators");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.administrators",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    // load admin for authentication
    adminForAuth =
        DatabaseSeeder.getSeedUserByRole("scenarioAdministrators", User.Role.ADMINISTRATOR);
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
  public void getAdministratorsFromAdmin() {
    // Get an admin
    User admin =
        DatabaseSeeder.getSeedUserByRole("scenarioAdministrators", User.Role.ADMINISTRATOR);

    List<URI> administratorsURIs =
        RestFactory.doGetRequest(admin, "administrators")
            .readEntity(AdministratorsResponse.class)
            .getAdministrators();

    print("GET /administrators");
    for (URI uri : administratorsURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getAdministratorsFromTeacher() {
    // Get a teacher
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioAdministrators", User.Role.TEACHER);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "administrators").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getAdministratorsFromParent() {
    // Get a parent
    User parent = DatabaseSeeder.getSeedUserByRole("scenarioAdministrators", User.Role.PARENT);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "administrators").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postAdministrators() {
    User administratorToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioAdministratorsToPost", User.Role.ADMINISTRATOR, 0);
    assertNotNull(administratorToPost);

    URI resourceURI =
        RestFactory.doPostRequest(adminForAuth, administratorToPost, "administrators");

    // check admin created
    Optional<User> createdAdminOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, administratorToPost.getEmail());
    assertTrue(createdAdminOpt.isPresent());

    print("POST /administrators");
    print(resourceURI);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getAdministratorById() {
    // Create a new User in the system
    User administratorToPost =
        DatabaseSeeder.getSeedUserByRole("scenarioAdministratorsToPost", User.Role.ADMINISTRATOR, 1);
    assertNotNull(administratorToPost);

    URI resourceURI =
        RestFactory.doPostRequest(adminForAuth, administratorToPost, "administrators");

    // Get the ID
    Path fullPath = Paths.get("/", resourceURI.getPath());
    String administratorId = fullPath.getParent().relativize(fullPath).toString();

    User administrator =
        RestFactory.doGetRequest(adminForAuth, "administrators", administratorId)
            .readEntity(User.class);

    assertTrue(administratorToPost.weakEquals(administrator));

    // Print it
    print("GET /administrators/", administratorId);
    print(administrator);
  }

}

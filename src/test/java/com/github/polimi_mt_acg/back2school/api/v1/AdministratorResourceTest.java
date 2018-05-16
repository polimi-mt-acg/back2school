package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorResponse;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class AdministratorResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioAdministrators");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.administrators");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.AuthEndpoint.class)
  public void getAdministrators() throws IOException {
    // Get administrators seeds
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioAdministrators", "users.json");

    // For each administrator
    for (User admin : admins) {
      // Create a get request
      Invocation request =
          RestFactory.getAuthenticatedInvocationBuilder(new String[] {"administrators"}, admin)
              .buildGet();

      // Invoke the request
      AdministratorResponse response = request.invoke(AdministratorResponse.class);

      // Performs assertions
      assertNotNull(response);
      for (Iterator<User> it = response.getAdministrators(); it.hasNext(); ) {
        User u = it.next();
        assertNotNull(u);
        assertEquals(u.getRole(), User.Role.ADMINISTRATOR);
        assertNotNull(u.getEmail());
        assertNotNull(u.getName());
        assertNotNull(u.getSurname());
      }

      // Print it
      System.out.println(
          new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
  }

  @Test(expected = NotAuthorizedException.class)
  @Category(TestCategory.AuthEndpoint.class)
  public void getAdministratorsUnauthorized() throws Exception {
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set target to /administrators
    target = target.path("administrators");

    // Build the GET request with no auth
    Invocation request = target.request(MediaType.APPLICATION_JSON).buildGet();

    // Invoke the request and expect Unauthorized exception
    request.invoke(AdministratorResponse.class);
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.AuthenticationEndpoint;
import com.github.polimi_mt_acg.back2school.api.v1.JacksonCustomMapper;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class AdministratorResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioAdministrators");

    // Run HTTP server
    server = startServer();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

    // Close HTTP server
    server.shutdownNow();
  }

  private static HttpServer startServer() {
    // Create a resource config that scans for JAX-RS resources and providers
    // in com.github.polimi_mt_acg.back2school.api.v1.administrators.resources package
    final ResourceConfig rc =
        new ResourceConfig()
            .register(AuthenticationEndpoint.class)
            .packages("com.github.polimi_mt_acg.back2school.api.v1.administrators")
            .register(JacksonCustomMapper.class)
            .register(JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
  }

  @Test
  public void getAdministrators() throws IOException {
    // Get Database seeds
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioAdministrators", "users.json");

    // For each administrator
    for (User admin : admins) {
      // Build the Client
      WebTarget target = RestFactory.buildWebTarget();
      // Authenticate
      String token = RestFactory.authenticate(admin.getEmail(), admin.getSeedPassword());
      assertNotNull(token);
      assertTrue(!token.isEmpty());

      // Set target to /administrators
      target = target.path("administrators");

      // Set token and build the GET request
      Invocation request =
          target
              .request(MediaType.APPLICATION_JSON)
              .header(HttpHeaders.AUTHORIZATION, token)
              .buildGet();

      // Invoke the request
      AdministratorResponse response = request.invoke(AdministratorResponse.class);

      // Performs assertions
      assertNotNull(response);
      for (User u : response.getAdministrators()) {
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
  public void getAdministratorsUnauthorized() throws Exception {
    // Get Database seeds
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioAdministrators", "users.json");

    // Get a sample administrator
    User admin = admins.get(0);

    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set target to /administrators
    target = target.path("administrators");

    // Build the GET request with no auth
    Invocation request = target.request(MediaType.APPLICATION_JSON).buildGet();

    // Invoke the request and expect Unauthorized exception
    AdministratorResponse response = request.invoke(AdministratorResponse.class);
  }
}

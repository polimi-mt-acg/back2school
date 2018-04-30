package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
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

public class NotificationsResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioNotifications");

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
            .packages("com.github.polimi_mt_acg.back2school.api.v1.notifications")
            .register(JacksonCustomMapper.class)
            .register(JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
  }

  @Test
  public void getNotifications() throws IOException {
    // Get Database seeds
    List<User> admins =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioNotifications", "users.json");

    // For each administrator
    for (User admin : admins) {
      if (admin.getRole() == Role.ADMINISTRATOR) {
        // Build the Client
        WebTarget target = RestFactory.buildWebTarget();
        // Authenticate
        String token = RestFactory.authenticate(admin.getEmail(), admin.getSeedPassword());
        assertNotNull(token);
        assertTrue(!token.isEmpty());

        // Set target to /notifications
        target = target.path("notifications");

        // Set token and build the GET request
        Invocation request =
            target
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .buildGet();

        // Invoke the request
        NotificationsResponse response = request.invoke(NotificationsResponse.class);

        // Print it
        ObjectMapper mapper = RestFactory.objectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
      }
    }
  }
}

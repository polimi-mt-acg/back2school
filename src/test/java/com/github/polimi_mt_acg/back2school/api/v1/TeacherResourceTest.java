package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.teachers.TeacherResponse;
import com.github.polimi_mt_acg.back2school.model.User;
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
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class TeacherResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioTeachers");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class, "com.github.polimi_mt_acg.back2school.api.v1.teachers");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  public void getTeachers() {}

  @Test
  @Category(TestCategory.Transient.class)
  public void getAdministrators() throws IOException {
    // Get seeds' users
    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioTeachers", "users.json");
    assertNotNull(seedUsers);

    // get the admin from seeds
    User seedAdmin =
        seedUsers
            .stream()
            .filter(user -> user.getRole().equals(User.Role.ADMINISTRATOR))
            .collect(Collectors.toList())
            .get(0);

    // get the teachers from seeds
    List<User> seedTeachers =
        seedUsers
            .stream()
            .filter(user -> user.getRole().equals(User.Role.TEACHER))
            .collect(Collectors.toList());

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(seedAdmin, "teachers")
            .buildGet();

    TeacherResponse response = request.invoke(TeacherResponse.class);
    assertNotNull(response);

    List<User> responseTeachers = response.getTeachers();

    // for each seed user check if it exist in the response
    for (User seedTeacher : seedTeachers) {
      // look for the the current seed user in the response
      User responseTeacher =
          responseTeachers
              .stream()
              .filter(user -> user.getEmail().equals(seedTeacher.getEmail()))
              .collect(Collectors.toList())
              .get(0);

      assertNotNull(responseTeacher);
      assertTrue(seedTeacher.weakEquals(responseTeacher));
    }

    // Print it
    System.out.println(
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
  }
}

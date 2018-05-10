package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomResponse;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectResponse;
import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
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

public class ClassroomsResourceTest {

    private static HttpServer server;

    @BeforeClass
    public static void setUp() throws Exception {
        // Deploy database scenario
        DatabaseSeeder.deployScenario("scenarioClassrooms");

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
        // in com.github.polimi_mt_acg.back2school.api.v1.subjects.resources package
        final ResourceConfig rc =
                new ResourceConfig()
                        .register(AuthenticationEndpoint.class)
                        .packages("com.github.polimi_mt_acg.back2school.api.v1.classrooms")
                        .register(JacksonCustomMapper.class)
                        .register(JacksonFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
    }

    @Test
    public void getClassrooms() throws IOException {
        // Get Database seeds
        List<User> admins =
                (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioClassrooms", "users.json");

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
                target = target.path("classrooms");

                // Set token and build the GET request
                Invocation request =
                        target
                                .request(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                                .buildGet();

                // Invoke the request
                ClassroomResponse response = request.invoke(ClassroomResponse.class);
                assertNotNull(response);

                // Print it
                ObjectMapper mapper = RestFactory.objectMapper();
                System.out.println("----CLASSROOMS----"+mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
            }
        }
    }

}

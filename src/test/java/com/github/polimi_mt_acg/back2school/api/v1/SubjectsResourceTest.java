package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.*;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.PutSubjectRequest;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectsResponse;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Subject;
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

public class SubjectsResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioSubjects");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.subjects",
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
  public void getSubjects() {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "subjects").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    //    System.out.println(admin);

    List<URI> subjectsURIs = response.readEntity(SubjectsResponse.class).getSubjects();

    for (URI uri : subjectsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void postSubjects() {

    URI resourceURI = postMate(0);

    System.out.println(resourceURI);
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void getSubjectID() throws JsonProcessingException {
    // Create a new Classroom in the system
    URI mateURI = postMate(1);
    System.out.println(mateURI);

    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    User teacher = get(Role.TEACHER);

    // mate ID
    Path fullPath = Paths.get("/", mateURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String mateID = idPath.toString();

    //    Administrator
    Invocation requestAdministrator =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "subjects", mateID).buildGet();

    Response responseAdministrator = requestAdministrator.invoke();
    assertEquals(Status.OK.getStatusCode(), responseAdministrator.getStatus());
    Subject mateResponseAdministrator = responseAdministrator.readEntity(Subject.class);

    assertTrue(weakEquals(mateResponseAdministrator, buildMate(1)));

    // Print it
    ObjectMapper mapperA = RestFactory.objectMapper();
    System.out.println(
        mapperA.writerWithDefaultPrettyPrinter().writeValueAsString(mateResponseAdministrator));

    //    Teacher
    Invocation requestTeacher =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "subjects", mateID).buildGet();

    Response responseTeacher = requestTeacher.invoke();
    assertEquals(Status.OK.getStatusCode(), responseTeacher.getStatus());
    Subject mateResponseTeacher = responseTeacher.readEntity(Subject.class);

    assertTrue(weakEquals(mateResponseTeacher, buildMate(1)));

    // Print it
    ObjectMapper mapperT = RestFactory.objectMapper();
    System.out.println(
        mapperT.writerWithDefaultPrettyPrinter().writeValueAsString(mateResponseTeacher));
  }

  @Test
  @Category(TestCategory.Transient.class)
  public void putSubjectById() throws JsonProcessingException {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    URI subjectURI = postMate(2);

    Invocation getSubject =
        RestFactory.getAuthenticatedInvocationBuilder(admin, subjectURI).buildGet();
    Subject subjectResponse = getSubject.invoke().readEntity(Subject.class);

    String nameSuffix = "newName";
    String descriptionSuffix = "newDescription";

    // Create a PutSubject request to change its name
    PutSubjectRequest putSubjectRequest = new PutSubjectRequest();
    putSubjectRequest.setName(subjectResponse.getName() + nameSuffix);
    putSubjectRequest.setDescription(subjectResponse.getDescription() + descriptionSuffix);

    // Make a PUT request
    Invocation putSubject =
        RestFactory.getAuthenticatedInvocationBuilder(admin, subjectURI)
            .buildPut(Entity.json(putSubjectRequest));
    Response putSubjectResponse = putSubject.invoke();

    assertEquals(Status.OK.getStatusCode(), putSubjectResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetSubject =
        RestFactory.getAuthenticatedInvocationBuilder(admin, subjectURI).buildGet();
    Subject newSubjectResponse = newGetSubject.invoke().readEntity(Subject.class);

    assertEquals(subjectResponse.getName() + nameSuffix, newSubjectResponse.getName());
    assertEquals(
        subjectResponse.getDescription() + descriptionSuffix, newSubjectResponse.getDescription());

    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newSubjectResponse));
  }

  private static HttpServer startServer() {
    // Create a resource config that scans for JAX-RS resources and providers
    // in com.github.polimi_mt_acg.back2school.api.v1.subjects.resources package
    final ResourceConfig rc =
        new ResourceConfig()
            .register(AuthenticationResource.class)
            .packages("com.github.polimi_mt_acg.back2school.api.v1.subjects")
            .register(JacksonCustomMapper.class)
            .register(JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
  }

  private Subject makeSubject() {
    Subject sbj = new Subject();
    sbj.setName("Filosofia");
    sbj.setDescription("Storia della filosofia");
    return sbj;
  }

  private User get(Role role) {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioSubjects", "users.json");

    List<User> usersWithRole =
        users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private URI postMate(int copyNumber) {
    // Create a new Subjects in the system
    Subject mate = buildMate(copyNumber);

    // Get an admin to register B11 in the system
    User admin = get(Role.ADMINISTRATOR);

    // Make a POST to /students
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "subjects")
            .buildPost(Entity.json(mate));

    Response response = post.invoke();
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  private Subject buildMate(int copyNumber) {
    Subject mate = new Subject();
    mate.setName("Matematica " + copyNumber);
    mate.setDescription("Analisi matematica" + copyNumber);
    return mate;
  }

  private boolean weakEquals(Subject u, Subject p) {
    return u.getName().equals(p.getName()) && u.getDescription().equals(p.getDescription());
  }
}

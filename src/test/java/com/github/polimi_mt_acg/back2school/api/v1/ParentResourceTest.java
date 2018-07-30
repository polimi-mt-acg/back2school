package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class ParentResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Truncate DB
//    DatabaseHandler.getInstance().truncateDatabase();
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.parents",
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
  @Category(TestCategory.ParentsEndpoint.class)
  public void getParentsFromAdmin() {
    // Get an admin
    User admin = get(User.Role.ADMINISTRATOR);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "parents").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    List<URI> parentsURIs = response.readEntity(ParentsResponse.class).getParents();
    for (URI uri : parentsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void getParentsFromParent() {
    // Get a parent
    User parent = get(User.Role.PARENT);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
//    System.out.println("ERROR "+response.getStatus());
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void postParents() {
    URI resourceURI = doParentPost(0,buildMarcos(0));

    System.out.println(resourceURI);
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void getParentByIdFromAdministrator() throws JsonProcessingException {
    User marcos = buildMarcos(1);
    URI postMarcos = doParentPost(1,marcos);
    User admin = get(User.Role.ADMINISTRATOR);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", postMarcos.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String marcosID = idPath.toString();
    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", marcosID).buildGet();

    Response response = request.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//    System.out.println("HERE1"+response.getStatus());
    User marcosResponse = response.readEntity(User.class);

    assertTrue(weakEquals(marcosResponse, marcos));

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(marcosResponse));
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void getParentByIdFromNotSameParent() throws JsonProcessingException {
    User seedParent =
            DatabaseSeeder.getSeedUserByRole("scenarioParents_ToPost1", User.Role.PARENT);
    assertNotNull(seedParent);

    URI postParent = doParentPost(2,seedParent);
    User admin = get(User.Role.ADMINISTRATOR);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", postParent.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User bob = get(User.Role.PARENT);
    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(bob, "parents", parentID).buildGet();

    Response response = request.invoke();
//    System.out.println("HERE1"+response.getStatus());

    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void getParentByIdFromSameParent() throws JsonProcessingException {
    User seedParent =
            DatabaseSeeder.getSeedUserByRole("scenarioParents_ToPost2", User.Role.PARENT);
    assertNotNull(seedParent);

    URI postParent = doParentPost(3,seedParent);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", postParent.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(seedParent, "parents", parentID).buildGet();

    Response response = request.invoke();
//    System.out.println("HERE1"+response.getStatus());

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    User parentResponse = response.readEntity(User.class);

    assertTrue(weakEquals(parentResponse, seedParent));

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentResponse));
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void putParentByIdFromAdmin() throws JsonProcessingException {
    // Get an admin
    User admin = get(User.Role.ADMINISTRATOR);

    User parent = buildMarcos(2);
    URI parentURI = doParentPost(4, parent);

    Invocation getParent =
            RestFactory.getAuthenticatedInvocationBuilder(admin, parentURI).buildGet();
    User parentResponse = getParent.invoke().readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutParent request to change its name
    PutParentRequest putParentRequest = new PutParentRequest();
    putParentRequest.setName(parentResponse.getName() + nameSuffix);
    putParentRequest.setSurname(parentResponse.getSurname() + surnameSuffix);
    putParentRequest.setEmail(parentResponse.getEmail() + emailSuffix);
    putParentRequest.setPassword("DontCare");

    // Make a PUT request
    Invocation putParent =
            RestFactory.getAuthenticatedInvocationBuilder(admin, parentURI)
                    .buildPut(Entity.json(putParentRequest));
    Response putParentResponse = putParent.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), putParentResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetParent =
            RestFactory.getAuthenticatedInvocationBuilder(admin, parentURI).buildGet();
    User newParentResponse = newGetParent.invoke().readEntity(User.class);

    assertEquals(parentResponse.getName() + nameSuffix, newParentResponse.getName());
    assertEquals(parentResponse.getSurname() + surnameSuffix, newParentResponse.getSurname());
    assertEquals(parentResponse.getEmail() + emailSuffix, newParentResponse.getEmail());

    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newParentResponse));
  }

  @Test
  @Category(TestCategory.ParentsEndpoint.class)
  public void putParentByIdFromSameParent() throws JsonProcessingException {
    // Get an admin
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(5, parent);
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();
    User admin = get(User.Role.ADMINISTRATOR);

    Invocation getParent =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID).buildGet();
    User parentResponse = getParent.invoke().readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutParent request to change its name
    PutParentRequest putParentRequest = new PutParentRequest();
    putParentRequest.setName(parentResponse.getName() + nameSuffix);
    putParentRequest.setSurname(parentResponse.getSurname() + surnameSuffix);
    putParentRequest.setEmail(parentResponse.getEmail() + emailSuffix);
    putParentRequest.setPassword("DontCare");

//    System.out.println("HERE"+ putParentRequest.getPassword() + putParentRequest.getEmail());

    // Make a PUT request
    Invocation putParent =
            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID).buildPut(Entity.json(putParentRequest));
    Response putParentResponse = putParent.invoke();

//    System.out.println("HERE"+ putParentResponse);
    assertEquals(Response.Status.OK.getStatusCode(), putParentResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetParent =
            RestFactory.getAuthenticatedInvocationBuilder(admin, parentURI).buildGet();
    User newParentResponse = newGetParent.invoke().readEntity(User.class);

    assertEquals(parentResponse.getName() + nameSuffix, newParentResponse.getName());
    assertEquals(parentResponse.getSurname() + surnameSuffix, newParentResponse.getSurname());
    assertEquals(parentResponse.getEmail() + emailSuffix, newParentResponse.getEmail());

    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newParentResponse));
  }


  private User get(User.Role role) {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithRole =
        users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private User getUserByID (String userID) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithID =
            users.stream().filter(user -> user.getId()== Integer.parseInt(userID)).collect(Collectors.toList());
    return usersWithID.get(0);
  }

  private User getUserByEmail(String email) {
    List<User> users =
        DatabaseHandler.getInstance().getListSelectFromWhereEqual(User.class, User_.email, email);

    return users.get(0);
  }

  private User buildCarlos(int copyNumber) {
    User carlos = new User();
    carlos.setName("Carlos " + copyNumber);
    carlos.setSurname("Hernandez " + copyNumber);
    carlos.setEmail("carlos.hernandez" + copyNumber + "@mail.com");
    carlos.setSeedPassword("carlos_password");
    carlos.setRole(User.Role.STUDENT);
    carlos.prepareToPersist();
    return carlos;
  }

  private User buildMarcos(int copyNumber) {
    User marcos = new User();
    marcos.setName("Marcos " + copyNumber);
    marcos.setSurname("Ferdinand " + copyNumber);
    marcos.setEmail("marcos.ferdinand" + copyNumber + "@mail.com");
    marcos.setSeedPassword("marcos_password");
    marcos.setRole(User.Role.PARENT);
    marcos.prepareToPersist();
    return marcos;
  }

  private User getAChild (int copynumber){
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");
    List<User> children =
            users
                    .stream()
                    .filter(user -> user.getRole().equals(User.Role.STUDENT))
                    .collect(Collectors.toList());
    return children.get(copynumber);
  }

  /**
   * Do a post an return the inserted parent URI.
   *
   * @return The inserted resource URI.
   */
  private URI doParentPost(int copynumber, User parent) {
    User child = getAChild(copynumber);
    String childEmail =child.getEmail();

    // Now build a PostParentRequest
    PostParentRequest request = new PostParentRequest();
    request.setParentAndPassword(parent, parent.getSeedPassword());
    request.setStudentEmail(childEmail);

    User admin = get(User.Role.ADMINISTRATOR);

    // Make a POST to /parents
    Invocation post =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents")
                    .buildPost(Entity.json(request));

    Response response = post.invoke();
    System.out.println("HERE POST REQ"+ response.toString());

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  private boolean weakEquals(User u, User p) {
    return u.getName().equals(p.getName())
        && u.getSurname().equals(p.getSurname())
        && u.getEmail().equals(p.getEmail());
  }
}
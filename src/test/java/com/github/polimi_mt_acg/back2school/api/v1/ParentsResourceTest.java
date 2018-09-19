package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
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

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.as_int;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

public class ParentsResourceTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Truncate DB
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.parents",
            "com.github.polimi_mt_acg.back2school.api.v1.students",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");

    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioParents", User.Role.ADMINISTRATOR);
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
  public void getParentsFromAdmin() {
    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    List<URI> parentsURIs = response.readEntity(ParentsResponse.class).getParents();
    for (URI uri : parentsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentsFromParent() {
    // Get a parent
    User parent = DatabaseSeeder.getSeedUserByRole("scenarioParents", User.Role.PARENT);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParents() {
    doParentPost(adminForAuth, buildMarcos(0));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentByIdFromAdministrator() {
    User marcos = buildMarcos(1);
    URI marcosURI = doParentPost(adminForAuth, marcos);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", marcosURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String marcosID = idPath.toString();
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", marcosID).buildGet();

    Response response = request.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    User marcosResponse = response.readEntity(User.class);

    assertTrue(marcosResponse.weakEquals(marcos));

    print("GET /parents", marcosID);
    print("Response: ");
    print(marcosResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentByIdFromNotSameParent() {
    User seedParent = DatabaseSeeder.getSeedUserByRole("scenarioParents_ToPost1", User.Role.PARENT);
    assertNotNull(seedParent);

    URI parentURI = doParentPost(adminForAuth, seedParent);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User bob = DatabaseSeeder.getSeedUserByRole("scenarioParents", User.Role.PARENT);
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(bob, "parents", parentID).buildGet();

    Response response = request.invoke();
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentByIdFromSameParent() {
    User seedParent = DatabaseSeeder.getSeedUserByRole("scenarioParents_ToPost2", User.Role.PARENT);
    assertNotNull(seedParent);

    URI postParent = doParentPost(adminForAuth, seedParent);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", postParent.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(seedParent, "parents", parentID).buildGet();

    Response response = request.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    User parentResponse = response.readEntity(User.class);

    assertTrue(parentResponse.weakEquals(seedParent));

    print("GET /parents", parentID);
    print("Response: ");
    print(parentResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putParentByIdFromAdmin() {
    User parent = buildMarcos(2);
    URI parentURI = doParentPost(adminForAuth, parent);

    Invocation getParent =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, parentURI).buildGet();
    User parentResponse = getParent.invoke().readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutParent request to change its name
    User putParent = new User();
    putParent.setName(parentResponse.getName() + nameSuffix);
    putParent.setSurname(parentResponse.getSurname() + surnameSuffix);
    putParent.setEmail(parentResponse.getEmail() + emailSuffix);
    putParent.setNewPassword("DontCare");

    // Make a PUT request
    Invocation putParentRequest =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, parentURI)
            .buildPut(Entity.json(putParent));
    Response putParentResponse = putParentRequest.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), putParentResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetParent =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, parentURI).buildGet();
    User newParentResponse = newGetParent.invoke().readEntity(User.class);

    assertEquals(parentResponse.getName() + nameSuffix, newParentResponse.getName());
    assertEquals(parentResponse.getSurname() + surnameSuffix, newParentResponse.getSurname());
    assertEquals(parentResponse.getEmail() + emailSuffix, newParentResponse.getEmail());

    print("GET ", parentURI);
    print("Response: ");
    print(newParentResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putParentByIdFromSameParent() {
    // Get an admin
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(adminForAuth, parent);
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation getParent =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentID).buildGet();
    User parentResponse = getParent.invoke().readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutParent request to change its name
    User putParent = new User();
    putParent.setName(parentResponse.getName() + nameSuffix);
    putParent.setSurname(parentResponse.getSurname() + surnameSuffix);
    putParent.setEmail(parentResponse.getEmail() + emailSuffix);
    putParent.setNewPassword("DontCare");

    // Make a PUT request
    Invocation putParentRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID)
            .buildPut(Entity.json(putParent));
    Response putParentResponse = putParentRequest.invoke();

    //    System.out.println("HERE"+ putParentResponse);
    assertEquals(Response.Status.OK.getStatusCode(), putParentResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetParent =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, parentURI).buildGet();
    User newParentResponse = newGetParent.invoke().readEntity(User.class);

    assertEquals(parentResponse.getName() + nameSuffix, newParentResponse.getName());
    assertEquals(parentResponse.getSurname() + surnameSuffix, newParentResponse.getSurname());
    assertEquals(parentResponse.getEmail() + emailSuffix, newParentResponse.getEmail());

    print("GET ", parentURI);
    print("Response: ");
    print(newParentResponse);
  }


  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentChildrenFromAdmin() {
    // add a parent to the database
    User parent = buildMarcos(8);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPathParent = Paths.get("/", parentURI.getPath());
    Path idPathParent = fullPathParent.getParent().relativize(fullPathParent);
    String parentId = idPathParent.toString();

    // get a child from the database
    User child =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.STUDENT).get(2);

    // create request to associate parent and child and invoke it
    ParentsChildrenRequest requestPost = new ParentsChildrenRequest();
    requestPost.setChildId(child.getId());
    Response postResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentId, "children")
            .buildPost(Entity.json(requestPost))
            .invoke();
    assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());

    // Now query /parents/{parent_id}/children from admin
    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentId, "children")
            .buildGet()
            .invoke();

    assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

    ParentChildrenResponse marcosChildren = getResponse.readEntity(ParentChildrenResponse.class);
    assertTrue(marcosChildren.getChildren().size() > 0);

    print("GET /parents/", parentId, "/children");
    print("Response: ");
    print(marcosChildren);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentChildrenFromAdmin() {
    // add a parent to the database
    User parent = buildMarcos(4);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPathParent = Paths.get("/", parentURI.getPath());
    Path idPathParent = fullPathParent.getParent().relativize(fullPathParent);
    String parentId = idPathParent.toString();

    // get a child from the database
    User child =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.STUDENT).get(6);

    // create request to associate parent and child and invoke it
    ParentsChildrenRequest requestPost = new ParentsChildrenRequest();
    requestPost.setChildId(child.getId());
    Response postResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentId, "children")
            .buildPost(Entity.json(requestPost))
            .invoke();
    assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());

    // now query /parents/{marco_id}/children from admin
    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentId, "children")
            .buildGet()
            .invoke();

    assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

    ParentChildrenResponse marcosChildren = getResponse.readEntity(ParentChildrenResponse.class);

    // and check its size is increased at more than 0
    assertTrue(marcosChildren.getChildren().size() > 0);

    print("GET /parents/", parentId, "/children");
    print("Response: ");
    print(marcosChildren);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentChildrenFromSameParent() {
    // add a parent to the database
    User parent = buildMarcos(5);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPathParent = Paths.get("/", parentURI.getPath());
    Path idPathParent = fullPathParent.getParent().relativize(fullPathParent);
    String parentId = idPathParent.toString();


    // get a child from the database
    User child =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.STUDENT).get(7);

    // create request to associate parent and child and invoke it
    ParentsChildrenRequest requestPost = new ParentsChildrenRequest();
    requestPost.setChildId(child.getId());
    Response postResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentId, "children")
            .buildPost(Entity.json(requestPost))
            .invoke();
    assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());

    // Now query /parents/{marco_id}/children from the parent
    Response getResponse =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentId, "children")
            .buildGet()
            .invoke();

    assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());

    ParentChildrenResponse marcosChildren = getResponse.readEntity(ParentChildrenResponse.class);

    assertTrue(marcosChildren.getChildren().size() > 0);

    print("GET /parents/", parentId, "/children");
    print("Response: ");
    print(marcosChildren);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentChildrenFromNotSameParent() {
    User parent = buildMarcos(6);
    URI parentURI = doParentPost(adminForAuth, parent);

    User secondParent = buildMarcos(7);
    doParentPost(adminForAuth, secondParent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    // Now query /parents/{marco_id}/children from admin
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(secondParent, "parents", parentID, "children")
            .buildGet();

    Response response = request.invoke();

    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  public static User buildMarcos(int copyNumber) {
    User marcos = new User();
    marcos.setName("Marcos " + copyNumber);
    marcos.setSurname("Ferdinand " + copyNumber);
    marcos.setEmail("marcos.ferdinand" + copyNumber + "@mail.com");
    marcos.setNewPassword("marcos_password");
    marcos.setRole(User.Role.PARENT);
    return marcos;
  }

  /**
   * Do a post an return the inserted parent URI.
   *
   * @return The inserted resource URI.
   */
  public static URI doParentPost(User userForAuth, User parent) {
    // Make a POST to /parents
    Invocation postRequest =
        RestFactory.getAuthenticatedInvocationBuilder(userForAuth, "parents")
            .buildPost(Entity.json(parent));

    Response postResponse = postRequest.invoke();
    print("POST /parents. Response location: ", postResponse.getLocation());

    assertEquals(Response.Status.CREATED.getStatusCode(), postResponse.getStatus());

    URI resourceURI = postResponse.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

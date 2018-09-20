package com.github.polimi_mt_acg.back2school.api.v1;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectsResponse;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class SubjectsResourceTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioSubjects");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.subjects",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioSubjects", User.Role.ADMINISTRATOR);
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
  public void getSubjects() {

    List<URI> subjectsURIs =
        RestFactory.doGetRequest(adminForAuth, "subjects")
            .readEntity(SubjectsResponse.class)
            .getSubjects();

    print("GET /subjects");
    for (URI uri : subjectsURIs) {
      assertNotNull(uri);
      print(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postSubjects() {
    Subject newSubject = buildSubject(0);
    URI subjectURI = RestFactory.doPostRequest(adminForAuth, newSubject, "subjects");

    print("POST /subjects");
    print(subjectURI.toString());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getSubjectID() {
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioSubjects", Role.TEACHER);

    // add a new subject
    Subject newSubject = buildSubject(1);
    URI subjectURI = RestFactory.doPostRequest(adminForAuth, newSubject, "subjects");

    // mate ID
    Path fullPath = Paths.get("/", subjectURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String subjectID = idPath.toString();

    // GET from administrator
    Subject subjectResponseAdministrator =
        RestFactory.doGetRequest(adminForAuth, "subjects", subjectID).readEntity(Subject.class);

    assertTrue(subjectResponseAdministrator.weakEquals(buildSubject(1)));

    // Print it
    print("GET /subjects/", subjectID, " -- from admin");
    print(subjectResponseAdministrator);

    // GET from teacher
    Subject subjectResponseTeacher =
        RestFactory.doGetRequest(teacher, "subjects", subjectID).readEntity(Subject.class);

    assertTrue(subjectResponseTeacher.weakEquals(buildSubject(1)));

    // Print it
    print("GET /subjects/", subjectID, " -- from teacher");
    print(subjectResponseTeacher);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putSubjectById() {
    // add a new subject
    Subject newSubject = buildSubject(2);
    URI subjectURI = RestFactory.doPostRequest(adminForAuth, newSubject, "subjects");

    Subject subject =
        RestFactory.doGetRequest(adminForAuth, subjectURI).readEntity(Subject.class);

    String suffix = " --changed";

    // Change the subject attributes in order to modify it on server
    subject.setName(subject.getName() + suffix);
    subject.setDescription(subject.getDescription() + suffix);

    // Make a PUT request
    RestFactory.doPutRequest(adminForAuth, subject, subjectURI);

    // Make a new GET to compare results
    Subject modifiedSubject =
        RestFactory.doGetRequest(adminForAuth, subjectURI).readEntity(Subject.class);


    assertEquals(subject.getName(), modifiedSubject.getName());
    assertEquals(subject.getDescription(), modifiedSubject.getDescription());

    print("PUT ", subjectURI);
    print(modifiedSubject);
  }

  private Subject buildSubject(int copyNumber) {
    Subject mate = new Subject();
    mate.setName("Mathematics " + copyNumber);
    mate.setDescription("Mathematical analysis " + copyNumber);
    return mate;
  }
}

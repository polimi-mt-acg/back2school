package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.students.PostStudentRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.PutStudentRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentGradesResponse;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentsResponse;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class StudentsResourceTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioStudents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.students",
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
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentsFromAdmin() {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "students").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    List<URI> studentsURIs = response.readEntity(StudentsResponse.class).getStudents();
    for (URI uri : studentsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentsFromTeacher() {
    // Get a teacher
    User teacher = get(Role.TEACHER);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "students").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentsFromParent() {
    // Get a teacher
    User parent = get(Role.PARENT);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "students").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void postStudents() {
    URI resourceURI = postCarlos(0);

    System.out.println(resourceURI);
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentByIdFromParentOfStudent() throws JsonProcessingException {
    // Create a new User in the system
    URI carlosURI = postCarlos(1);
    System.out.println(carlosURI);

    // Now get Carlos' dad and query the student
    User dad = getCarlosDad();

    // Carlos ID
    Path fullPath = Paths.get("/", carlosURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String carlosID = idPath.toString();

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(dad, "students", carlosID).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    User carlosResponse = response.readEntity(User.class);

    assertTrue(weakEquals(carlosResponse, buildCarlos(1)));

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(carlosResponse));
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentByIdFromTeacherOfStudent() throws JsonProcessingException {
    User alice = getUserByEmail("alice@email.com");
    User aliceTeacher = getAliceTeacher();

    // Now query /students/{alice_id} from aliceTeacher
    String aliceID = String.valueOf(alice.getId());
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(aliceTeacher, "students", aliceID).buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    User aliceResponse = response.readEntity(User.class);

    assertTrue(weakEquals(aliceResponse, alice));

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aliceResponse));
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentByIdFromNotTeacherOfStudent() {
    User bob = getUserByEmail("bob@email.com");
    User notBobsTeacher = getNotBobsTeacher();

    // Now query /students/{alice_id} from aliceTeacher
    String aliceID = String.valueOf(bob.getId());
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(notBobsTeacher, "students", aliceID)
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void putStudentById() throws JsonProcessingException {
    // Get an admin
    User admin = get(Role.ADMINISTRATOR);

    URI studentURI = postCarlos(2);

    Invocation getStudent =
        RestFactory.getAuthenticatedInvocationBuilder(admin, studentURI).buildGet();
    User studentResponse = getStudent.invoke().readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutStudent request to change its name
    PutStudentRequest putStudentRequest = new PutStudentRequest();
    putStudentRequest.setName(studentResponse.getName() + nameSuffix);
    putStudentRequest.setSurname(studentResponse.getSurname() + surnameSuffix);
    putStudentRequest.setEmail(studentResponse.getEmail() + emailSuffix);
    putStudentRequest.setPassword("DontCare");

    // Make a PUT request
    Invocation putStudent =
        RestFactory.getAuthenticatedInvocationBuilder(admin, studentURI)
            .buildPut(Entity.json(putStudentRequest));
    Response putStudentResponse = putStudent.invoke();

    assertEquals(Status.OK.getStatusCode(), putStudentResponse.getStatus());

    // Make a new GET to compare results
    Invocation newGetStudent =
        RestFactory.getAuthenticatedInvocationBuilder(admin, studentURI).buildGet();
    User newStudentResponse = newGetStudent.invoke().readEntity(User.class);

    assertEquals(studentResponse.getName() + nameSuffix, newStudentResponse.getName());
    assertEquals(studentResponse.getSurname() + surnameSuffix, newStudentResponse.getSurname());
    assertEquals(studentResponse.getEmail() + emailSuffix, newStudentResponse.getEmail());

    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(
        mapper.writerWithDefaultPrettyPrinter().writeValueAsString(newStudentResponse));
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void getStudentGrades() throws JsonProcessingException {
    User alice = getUserByEmail("alice@email.com");
    User aliceTeacher = getAliceTeacher();

    // Now query /students/{alice_id}/grades from aliceTeacher
    String aliceID = String.valueOf(alice.getId());
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(aliceTeacher, "students", aliceID, "grades")
            .buildGet();

    Response response = request.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());
    StudentGradesResponse aliceGrades = response.readEntity(StudentGradesResponse.class);

    assertEquals(2, aliceGrades.getGrades().size());

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aliceGrades));
  }

  private User get(Role role) {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioStudents", "users.json");

    List<User> usersWithRole =
        users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private User getCarlosDad() {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioStudents", "users.json");

    List<User> carlosDads =
        users
            .stream()
            .filter(user -> user.getSurname().equals("SurnameCarlos"))
            .collect(Collectors.toList());
    return carlosDads.get(0);
  }

  private User getUserByEmail(String email) {
    List<User> users =
        DatabaseHandler.getInstance().getListSelectFromWhereEqual(User.class, User_.email, email);

    return users.get(0);
  }

  private User getAliceTeacher() {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioStudents", "users.json");

    List<User> aliceTeachers =
        users
            .stream()
            .filter(user -> user.getEmail().equals("alice.teacher@email.com"))
            .collect(Collectors.toList());
    return aliceTeachers.get(0);
  }

  private User getNotBobsTeacher() {
    List<User> users =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioStudents", "users.json");

    List<User> notBobsTeachers =
        users
            .stream()
            .filter(user -> user.getEmail().equals("notbobs.teacher@email.com"))
            .collect(Collectors.toList());
    return notBobsTeachers.get(0);
  }

  private User buildCarlos(int copyNumber) {
    User carlos = new User();
    carlos.setName("Carlos " + copyNumber);
    carlos.setSurname("Hernandez " + copyNumber);
    carlos.setEmail("carlos.hernandez" + copyNumber + "@mail.com");
    carlos.setSeedPassword("carlos_password");
    carlos.setRole(Role.STUDENT);
    carlos.prepareToPersist();
    return carlos;
  }

  private URI postCarlos(int copyNumber) {
    // Create a new User in the system
    User carlos = buildCarlos(copyNumber);

    // Get Carlos' dad email:
    User dad = getCarlosDad();
    String email = dad.getEmail();

    // Get an admin to register Carlos in the system
    User admin = get(Role.ADMINISTRATOR);

    // Now build a PostStudentRequest
    PostStudentRequest request = new PostStudentRequest();
    request.setStudent(carlos);
    request.setParentEmail(email);

    // Make a POST to /students
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(admin, "students")
            .buildPost(Entity.json(request));

    Response response = post.invoke();
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

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

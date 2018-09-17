package com.github.polimi_mt_acg.back2school.api.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.students.PostGradeRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.PostStudentRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.PutStudentRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentGradesResponse;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentsResponse;
import com.github.polimi_mt_acg.back2school.model.Grade;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.TestCategory.StudentsEndpoint;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    DatabaseHandler.getInstance().destroy();

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

    assertTrue(aliceGrades.getGrades().size() > 0);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(aliceGrades));
  }

  @Test
  @Category(TestCategory.StudentsEndpoint.class)
  public void postStudentGrade() {
    URI gradeURI = postGrade(0);

    System.out.println(gradeURI);
  }

  @Test
  @Category(StudentsEndpoint.class)
  public void getStudentGradeById() {
    // Fetch any English grade
    List<Grade> grades = DatabaseHandler.getInstance().getListSelectFrom(Grade.class);
    Grade englishGrade =
        grades
            .stream()
            .filter(grade -> grade.getSubject().getName().equals("English"))
            .collect(Collectors.toList())
            .get(0);
    String gradeId = String.valueOf(englishGrade.getId());
    String studentId = String.valueOf(englishGrade.getStudent().getId());

    // Get Alice teacher
    User aliceTeacher = getAliceTeacher();

    // Create Get request
    Invocation get =
        RestFactory.getAuthenticatedInvocationBuilder(
                aliceTeacher, "students", studentId, "grades", gradeId)
            .buildGet();

    Response response = get.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    Grade serverGrade = response.readEntity(Grade.class);
    assertNotNull(serverGrade);
    assertEquals(englishGrade.getTitle(), serverGrade.getTitle());
    assertEquals(englishGrade.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(englishGrade.getDate(), serverGrade.getDate());
    assertEquals(englishGrade.getSubject().getName(), serverGrade.getSubject().getName());
  }

  @Test
  @Category(StudentsEndpoint.class)
  public void putStudentGradeById() {
    // Fetch any English grade
    List<Grade> grades = DatabaseHandler.getInstance().getListSelectFrom(Grade.class);
    Grade englishGrade =
        grades
            .stream()
            .filter(grade -> grade.getSubject().getName().equals("English"))
            .collect(Collectors.toList())
            .get(0);
    String gradeId = String.valueOf(englishGrade.getId());
    String studentId = String.valueOf(englishGrade.getStudent().getId());

    // Get Alice teacher
    User aliceTeacher = getAliceTeacher();

    // Create a PostGrade request to modify the english grade
    PostGradeRequest request = new PostGradeRequest();
    request.setSubjectName(englishGrade.getSubject().getName());
    request.setDate(LocalDate.now());
    request.setTitle(englishGrade.getTitle());
    request.setGrade(10);

    // Create PUT request
    Invocation put =
        RestFactory.getAuthenticatedInvocationBuilder(
            aliceTeacher, "students", studentId, "grades", gradeId)
            .buildPut(Entity.json(request));

    Response response = put.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    // Create Get request
    Invocation get =
        RestFactory.getAuthenticatedInvocationBuilder(
            aliceTeacher, "students", studentId, "grades", gradeId)
            .buildGet();

    Response getResponse = get.invoke();
    assertEquals(Status.OK.getStatusCode(), getResponse.getStatus());

    Grade serverGrade = getResponse.readEntity(Grade.class);
    assertNotNull(serverGrade);
    assertEquals(request.getTitle(), serverGrade.getTitle());
    assertEquals(request.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(request.getDate(), serverGrade.getDate());
    assertEquals(request.getSubjectName(), serverGrade.getSubject().getName());
  }

  @Test
  @Category(StudentsEndpoint.class)
  public void deleteStudentGradeById() {
    // Fetch any English grade
    List<Grade> grades = DatabaseHandler.getInstance().getListSelectFrom(Grade.class);
    Grade englishGrade =
        grades
            .stream()
            .filter(grade -> grade.getSubject().getName().equals("English"))
            .collect(Collectors.toList())
            .get(0);
    String gradeId = String.valueOf(englishGrade.getId());
    String studentId = String.valueOf(englishGrade.getStudent().getId());

    // Get Alice teacher
    User aliceTeacher = getAliceTeacher();

    // Create Get request
    Invocation delete =
        RestFactory.getAuthenticatedInvocationBuilder(
            aliceTeacher, "students", studentId, "grades", gradeId)
            .buildDelete();

    Response response = delete.invoke();
    assertEquals(Status.OK.getStatusCode(), response.getStatus());

    // Now try to GET it from server and expect a NOT_FOUND message
    Invocation get = RestFactory.getAuthenticatedInvocationBuilder(
        aliceTeacher, "students", studentId, "grades", gradeId)
        .buildGet();
    Response notFoundResponse = get.invoke();
    assertEquals(Status.NOT_FOUND.getStatusCode(), notFoundResponse.getStatus());
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

  private User buildCarlos(int copyNumber) {
    User carlos = new User();
    carlos.setName("Carlos " + copyNumber);
    carlos.setSurname("Hernandez " + copyNumber);
    carlos.setEmail("carlos.hernandez" + copyNumber + "@mail.com");
    carlos.setNewPassword("carlos_password");
    carlos.setRole(Role.STUDENT);
    carlos.prepareToPersist();
    return carlos;
  }

  private URI postGrade(int copyNumber) {
    // Create a new Grade in the system
    PostGradeRequest grade = buildGrade(copyNumber);

    // Get Alice teacher
    User teacher = getAliceTeacher();

    // Get Alice ID
    User alice = getUserByEmail("alice@email.com");
    String aliceId = String.valueOf(alice.getId());

    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "students", aliceId, "grades")
            .buildPost(Entity.json(grade));

    Response response = post.invoke();
    assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);

    Invocation getGrade =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, resourceURI).buildGet();
    Grade serverGrade = getGrade.invoke().readEntity(Grade.class);

    assertNotNull(serverGrade);
    assertEquals(grade.getTitle(), serverGrade.getTitle());
    assertEquals(grade.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(grade.getDate(), serverGrade.getDate());
    assertEquals(grade.getSubjectName(), serverGrade.getSubject().getName());

    return resourceURI;
  }

  private PostGradeRequest buildGrade(int copyNumber) {
    PostGradeRequest grade = new PostGradeRequest();
    grade.setTitle("REST API project " + copyNumber);
    grade.setGrade(32);
    grade.setDate(LocalDate.now());
    grade.setSubjectName("Middleware Technologies");
    return grade;
  }

  private boolean weakEquals(User u, User p) {
    return u.getName().equals(p.getName())
        && u.getSurname().equals(p.getSurname())
        && u.getEmail().equals(p.getEmail());
  }
}

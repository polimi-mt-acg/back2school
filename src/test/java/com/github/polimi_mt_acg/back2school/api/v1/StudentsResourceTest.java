package com.github.polimi_mt_acg.back2school.api.v1;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentsChildrenRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentGradeRequest;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentGradesResponse;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentsResponse;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioStudents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.students",
            "com.github.polimi_mt_acg.back2school.api.v1.parents",
            "com.github.polimi_mt_acg.back2school.api.v1.subjects",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioStudents", User.Role.ADMINISTRATOR);
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
  public void getStudentsFromAdmin() {
    // Get an admin
    User admin = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.ADMINISTRATOR);

    List<URI> studentsURIs =
        RestFactory.doGetRequest(admin, "students")
            .readEntity(StudentsResponse.class)
            .getStudents();

    for (URI uri : studentsURIs) {
      assertNotNull(uri);
      System.out.println(uri);
    }
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentsFromTeacher() {
    // Get a teacher
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(teacher, "students").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentsFromParent() {
    // Get a teacher
    User parent = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.PARENT);

    Invocation getRequest =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "students").buildGet();

    Response response = getRequest.invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postStudents() {
    User carlos = buildCarlos(0);
    URI resourceURI = RestFactory.doPostRequest(adminForAuth, carlos, "students");
    print(resourceURI);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentByIdFromParentOfStudent() {
    // Create a new User in the system
    User carlos = buildCarlos(1);
    URI carlosURI = RestFactory.doPostRequest(adminForAuth, carlos, "students");
    print(carlosURI);

    // Now get Carlos' dad and then query the student
    User dad = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.PARENT, 0);
    assertNotNull(dad);
    assertEquals("carlos_dad.parent@email.com", dad.getEmail());

    // Carlos ID
    Path fullPath = Paths.get("/", carlosURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String carlosID = idPath.toString();

    // But first associate parent to the student child
    Optional<User> dadOpt = DatabaseHandler.fetchEntityBy(User.class, User_.email, dad.getEmail());
    assertTrue(dadOpt.isPresent());

    ParentsChildrenRequest requestPost = new ParentsChildrenRequest();
    requestPost.setChildId(Integer.valueOf(carlosID));
    String dadID = str(dadOpt.get().getId());
    Response postResponse =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", dadID, "children")
            .buildPost(Entity.json(requestPost))
            .invoke();
    assertEquals(Response.Status.OK.getStatusCode(), postResponse.getStatus());

    // now query the student being it's father
    User carlosResponse =
        RestFactory.doGetRequest(dad, "students", carlosID).readEntity(User.class);

    assertTrue(carlosResponse.weakEquals(buildCarlos(1)));

    // Print it
    print("GET /students", carlosID);
    print(carlosResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentByIdFromTeacherOfStudent() {
    Optional<User> aliceOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice@email.com");
    assertTrue(aliceOpt.isPresent());
    User alice = aliceOpt.get();

    User aliceTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(aliceTeacher);
    assertEquals("alice.teacher@email.com", aliceTeacher.getEmail());

    // Now query /students/{alice_id} from aliceTeacher
    User aliceResponse =
        RestFactory.doGetRequest(aliceTeacher, "students", alice.getId()).readEntity(User.class);

    assertTrue(aliceResponse.weakEquals(alice));

    // Print it
    print("GET /students/", alice.getId());
    print(aliceResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentByIdFromNotTeacherOfStudent() {
    Optional<User> bobOpt = DatabaseHandler.fetchEntityBy(User.class, User_.email, "bob@email.com");
    assertTrue(bobOpt.isPresent());
    User bob = bobOpt.get();

    User notBobsTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 1);
    assertNotNull(notBobsTeacher);
    assertEquals("notbobs.teacher@email.com", notBobsTeacher.getEmail());

    // Now query /students/{alice_id} from aliceTeacher
    String bobID = String.valueOf(bob.getId());
    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(notBobsTeacher, "students", bobID).buildGet();

    Response response = request.invoke();
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void putStudentById() {
    // Get an admin
    User carlos = buildCarlos(2);
    URI studentURI = RestFactory.doPostRequest(adminForAuth, carlos, "students");
    print(studentURI);

    User studentResponse =
        RestFactory.doGetRequest(adminForAuth, studentURI).readEntity(User.class);

    String nameSuffix = "newName";
    String surnameSuffix = "newSurname";
    String emailSuffix = "newEmail";

    // Create a PutStudent request to change its name
    User putStudent = new User();
    putStudent.setName(studentResponse.getName() + nameSuffix);
    putStudent.setSurname(studentResponse.getSurname() + surnameSuffix);
    putStudent.setEmail(studentResponse.getEmail() + emailSuffix);
    putStudent.setNewPassword("DontCare");

    // Make a PUT request
    RestFactory.doPutRequest(adminForAuth, putStudent, studentURI);

    // Make a new GET to compare results
    User newStudentResponse =
        RestFactory.doGetRequest(adminForAuth, studentURI).readEntity(User.class);

    assertEquals(studentResponse.getName() + nameSuffix, newStudentResponse.getName());
    assertEquals(studentResponse.getSurname() + surnameSuffix, newStudentResponse.getSurname());
    assertEquals(studentResponse.getEmail() + emailSuffix, newStudentResponse.getEmail());

    print("GET ", studentURI.toString());
    print(newStudentResponse);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getStudentGrades() {
    Optional<User> aliceOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice@email.com");
    assertTrue(aliceOpt.isPresent());
    User alice = aliceOpt.get();

    // Get Alice teacher
    User aliceTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(aliceTeacher);
    assertEquals("alice.teacher@email.com", aliceTeacher.getEmail());

    // Now query /students/{alice_id}/grades from aliceTeacher
    String aliceID = String.valueOf(alice.getId());
    StudentGradesResponse aliceGrades =
        RestFactory.doGetRequest(aliceTeacher, "students", aliceID, "grades")
            .readEntity(StudentGradesResponse.class);

    assertTrue(aliceGrades.getGrades().size() > 0);

    // Print it
    print("GET /students/", aliceID, "/grades");
    print(aliceGrades);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postStudentGrade() {
    URI gradeURI = postGrade(0);

    System.out.println(gradeURI);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
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
    User aliceTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(aliceTeacher);
    assertEquals("alice.teacher@email.com", aliceTeacher.getEmail());

    // Do Get request
    Grade serverGrade =
        RestFactory.doGetRequest(aliceTeacher, "students", studentId, "grades", gradeId)
            .readEntity(Grade.class);

    assertNotNull(serverGrade);
    assertEquals(englishGrade.getTitle(), serverGrade.getTitle());
    assertEquals(englishGrade.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(englishGrade.getDate(), serverGrade.getDate());
    assertEquals(englishGrade.getSubject().getName(), serverGrade.getSubject().getName());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
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
    User aliceTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(aliceTeacher);
    assertEquals("alice.teacher@email.com", aliceTeacher.getEmail());

    // Create a PostGrade request to modify the english grade
    StudentGradeRequest request = new StudentGradeRequest();
    request.setSubjectId(englishGrade.getSubject().getId());
    request.setDate(LocalDate.now());
    request.setTitle(englishGrade.getTitle());
    request.setGrade(10);

    // Create PUT request
    RestFactory.doPutRequest(aliceTeacher, request, "students", studentId, "grades", gradeId);

    // Create Get request
    Grade serverGrade =
        RestFactory.doGetRequest(aliceTeacher, "students", studentId, "grades", gradeId)
            .readEntity(Grade.class);

    assertNotNull(serverGrade);
    assertEquals(request.getTitle(), serverGrade.getTitle());
    assertEquals(request.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(request.getDate(), serverGrade.getDate());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
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
    User aliceTeacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(aliceTeacher);
    assertEquals("alice.teacher@email.com", aliceTeacher.getEmail());

    // Create Get request
    RestFactory.doDeleteRequest(aliceTeacher, "students", studentId, "grades", gradeId);

    // Now try to GET it from server and expect a NOT_FOUND message
    Invocation get =
        RestFactory.getAuthenticatedInvocationBuilder(
                aliceTeacher, "students", studentId, "grades", gradeId)
            .buildGet();
    Response notFoundResponse = get.invoke();
    assertEquals(Status.NOT_FOUND.getStatusCode(), notFoundResponse.getStatus());
  }

  private User buildCarlos(int copyNumber) {
    User carlos = new User();
    carlos.setRole(Role.STUDENT);
    carlos.setName("Carlos " + copyNumber);
    carlos.setSurname("Hernandez " + copyNumber);
    carlos.setEmail("carlos.hernandez" + copyNumber + "@mail.com");
    carlos.setNewPassword("carlos_password");
    return carlos;
  }

  private URI postGrade(int copyNumber) {
    // Create a new Grade in the system
    StudentGradeRequest grade = buildGrade(copyNumber);

    // Get Alice teacher
    User teacher = DatabaseSeeder.getSeedUserByRole("scenarioStudents", Role.TEACHER, 0);
    assertNotNull(teacher);
    assertEquals("alice.teacher@email.com", teacher.getEmail());

    // Get Alice ID
    Optional<User> aliceOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "alice@email.com");
    assertTrue(aliceOpt.isPresent());
    User alice = aliceOpt.get();

    String aliceId = String.valueOf(alice.getId());

    URI resourceURI = RestFactory.doPostRequest(teacher, grade, "students", aliceId, "grades");

    Grade serverGrade = RestFactory.doGetRequest(teacher, resourceURI).readEntity(Grade.class);

    assertNotNull(serverGrade);
    assertEquals(grade.getTitle(), serverGrade.getTitle());
    assertEquals(grade.getGrade(), serverGrade.getGrade(), 0.0);
    assertEquals(grade.getDate(), serverGrade.getDate());

    return resourceURI;
  }

  private StudentGradeRequest buildGrade(int copyNumber) {
    StudentGradeRequest grade = new StudentGradeRequest();
    grade.setTitle("REST API project " + copyNumber);
    grade.setGrade(32);
    grade.setDate(LocalDate.now());

    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(Subject.class, Subject_.name, "Middleware Technologies");
    assertTrue(subjectOpt.isPresent());
    grade.setSubjectId(subjectOpt.get().getId());
    return grade;
  }
}

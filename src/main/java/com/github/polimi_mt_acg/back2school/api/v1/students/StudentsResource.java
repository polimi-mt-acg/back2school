package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.*;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Grade;
import com.github.polimi_mt_acg.back2school.model.Grade_;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.hibernate.Session;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

@Path("students")
public class StudentsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getStudents(@Context UriInfo uriInfo) {
    // Get students from DB
    List<User> students =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.STUDENT);

    // For each user, build a URI to /students/{id}
    List<URI> uris = new ArrayList<>();
    for (User student : students) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(student.getId())).build();
      uris.add(uri);
    }

    StudentsResponse response = new StudentsResponse();
    response.setStudents(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postStudents(User newUser, @Context UriInfo uriInfo) {
    if (!newUser.isValidForPost()) return newUser.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // force to be a student since this endpoint meaning
    newUser.setRole(Role.STUDENT);
    newUser.prepareToPersist();

    session.persist(newUser);
    session.getTransaction().commit();
    session.close();

    // Now the student has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(str(newUser.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{studentId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentById(@PathParam("studentId") Integer studentId) {
    // Fetch User
    Optional<User> studentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, studentId);
    if (!studentOpt.isPresent() || !studentOpt.get().getRole().equals(Role.STUDENT)) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }
    return Response.ok(studentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{studentId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @ParentOfStudentSecured
  public Response putStudentById(User newUser, @PathParam("studentId") Integer studentId) {
    if (!newUser.isValidForPut(studentId)) return newUser.getInvalidPutResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User student = session.get(User.class, studentId);
    if (student == null || !student.getRole().equals(Role.STUDENT)) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }

    // Update student fields
    student.setName(newUser.getName());
    student.setSurname(newUser.getSurname());
    student.setEmail(newUser.getEmail());
    student.setPassword(newUser.getNewPassword());

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{studentId: [0-9]+}/grades")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentGrades(
      @PathParam("studentId") Integer studentId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch User
    User student = session.get(User.class, studentId);
    if (student == null || !student.getRole().equals(Role.STUDENT)) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }

    // Fetch grades of student
    List<Grade> grades =
        dbi.getListSelectFromWhereEqual(Grade.class, Grade_.student, student, session);
    StudentGradesResponse response = new StudentGradesResponse();
    List<URI> gradeURIs = new ArrayList<>();

    for (Grade g : grades) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(g.getId())).build();
      gradeURIs.add(uri);
    }
    response.setGrades(gradeURIs);

    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{studentId: [0-9]+}/grades")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response postStudentGrades(
      StudentGradeRequest gradeRequest,
      @PathParam("studentId") Integer studentId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    if (!gradeRequest.isValidForPost()) return gradeRequest.getInvalidPostResponse();

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get teacher/admin who made the request
    User teacher = AuthenticationSession.getCurrentUser(httpHeaders, session);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.UNAUTHORIZED)
          .entity(new StatusResponse(Status.UNAUTHORIZED, "No user logged in"))
          .build();
    }

    // Fetch student
    User student = session.get(User.class, studentId);
    if (student == null || !student.getRole().equals(Role.STUDENT)) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }

    // Fetch subject
    Subject subject = session.get(Subject.class, gradeRequest.getSubjectId());
    if (subject == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown subject id"))
          .build();
    }

    // Build the Grade entity
    Grade grade = new Grade();
    grade.setSubject(subject);
    grade.setTeacher(teacher);
    grade.setStudent(student);
    grade.setDate(gradeRequest.getDate());
    grade.setTitle(gradeRequest.getTitle());
    grade.setGrade(gradeRequest.getGrade());
    grade.prepareToPersist();

    session.persist(grade);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(grade.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{studentId: [0-9]+}/grades/{grade_id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentGradeById(
      @PathParam("studentId") Integer studentId, @PathParam("grade_id") Integer gradeId) {
    // Fetch student
    Optional<User> studentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, studentId);

    if (!studentOpt.isPresent() || !studentOpt.get().getRole().equals(Role.STUDENT)) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }
    // Fetch grade
    Optional<Grade> gradeOpt = DatabaseHandler.fetchEntityBy(Grade.class, Grade_.id, gradeId);
    if (!gradeOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown grade id"))
          .build();
    }

    return Response.ok(gradeOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{studentId: [0-9]+}/grades/{gradeId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response putStudentGradeById(
      StudentGradeRequest gradeRequest,
      @PathParam("studentId") Integer studentId,
      @PathParam("gradeId") Integer gradeId,
      @Context HttpHeaders httpHeaders) {
    if (!gradeRequest.isValidForPut(gradeId)) return gradeRequest.getInvalidPutResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get teacher/admin who made the request
    User teacher = AuthenticationSession.getCurrentUser(httpHeaders, session);
    if (teacher == null) { // not check if teacher because also admin can access
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.UNAUTHORIZED)
          .entity(new StatusResponse(Status.UNAUTHORIZED, "No user logged in"))
          .build();
    }

    // Fetch student
    User student = session.get(User.class, studentId);
    if (student == null || !student.getRole().equals(Role.STUDENT)) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }

    // Fetch subject
    Subject subject = session.get(Subject.class, gradeRequest.getSubjectId());
    if (subject == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown subject id"))
          .build();
    }

    // Fetch previous grade entity
    Grade grade = session.get(Grade.class, gradeId);

    // Check if user that created this grade is the same
    if (teacher.getRole().equals(Role.TEACHER) && teacher.getId() != grade.getTeacher().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.FORBIDDEN)
          .entity(
              new StatusResponse(
                  Status.FORBIDDEN, "Only the teacher that created the grade can modify it"))
          .build();
    }

    // Update grade fields
    grade.setSubject(subject);
    grade.setTeacher(teacher);
    grade.setStudent(student);
    grade.setDate(gradeRequest.getDate());
    grade.setTitle(gradeRequest.getTitle());
    grade.setGrade(gradeRequest.getGrade());
    grade.prepareToPersist();

    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{studentId: [0-9]+}/grades/{gradeId: [0-9]+}")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response deleteStudentGradeById(
      @PathParam("studentId") Integer studentId,
      @PathParam("gradeId") Integer gradeId,
      @Context HttpHeaders httpHeaders) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders);

    // Fetch student
    User student = session.get(User.class, studentId);
    if (student == null || !student.getRole().equals(Role.STUDENT)) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student id"))
          .build();
    }

    // Fetch Grade entity
    Grade grade = session.get(Grade.class, gradeId);
    if (grade == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown grade id"))
          .build();
    }

    // if it's a teacher deleting, check it is the same created the grade
    if (currentUser.getRole().equals(Role.TEACHER)
        && grade.getTeacher().getId() != currentUser.getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.FORBIDDEN)
          .entity(new StatusResponse(Status.FORBIDDEN, "Action not allowed"))
          .build();
    }

    // Delete Grade
    session.delete(grade);
    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }
}

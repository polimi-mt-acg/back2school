package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentTeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Grade;
import com.github.polimi_mt_acg.back2school.model.Grade_;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.Subject_;
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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.hibernate.Session;

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
  @AdministratorSecured
  public Response postStudents(PostStudentRequest request, @Context UriInfo uriInfo) {
    User student = request.getStudent();
    String parentEmail = request.getParentEmail();

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Check if a user with same email already exists, if so, do nothing
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, student.getEmail(), session);

    if (userOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity("A user with this email already exists.")
          .build();
    }
    // force to be a parent since this endpoint meaning
    student.setRole(Role.STUDENT);

    // Otherwise we accept the request. First we fetch Parent entity
    List<User> parentRes =
        dbi.getListSelectFromWhereEqual(User.class, User_.email, parentEmail, session);
    if (parentRes.isEmpty()) {
      // User with parentEmail email not found
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST).entity("Unregistered parent email.").build();
    }

    User parent = parentRes.get(0);
    if (parent.getRole() != Role.PARENT) {
      // parentEmail does not belong to a PARENT
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST).entity("Not a parent.").build();
    }

    parent.addChild(student);
    session.getTransaction().commit(); // Makes student persisted.
    session.close();

    // Now student has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(student.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentById(@PathParam("id") String studentId) {
    // Fetch User
    Optional<User> studentOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, Integer.parseInt(studentId));
    if (!studentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }
    return Response.ok(studentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @ParentOfStudentSecured
  public Response putStudentById(PutStudentRequest newStudent, @PathParam("id") String studentId) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User student = session.get(User.class, Integer.parseInt(studentId));
    if (student == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    // Update student fields
    student.setName(newStudent.getName());
    student.setSurname(newStudent.getSurname());
    student.setEmail(newStudent.getEmail());
    student.setPassword(newStudent.getPassword());

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().build();
  }

  @Path("{id: [0-9]+}/grades")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentGrades(@PathParam("id") String studentId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch User
    User student = session.get(User.class, Integer.parseInt(studentId));
    if (student == null) {
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
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

  @Path("{id: [0-9]+}/grades")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response postStudentGrades(
      PostGradeRequest request,
      @PathParam("id") String studentId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get teacher who made the request
    User teacher = AuthenticationSession.getCurrentUser(crc, session);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).build();
    }

    // Fetch student
    User student = session.get(User.class, Integer.parseInt(studentId));
    if (student == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    // Fetch the subject entity by name
    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(
            Subject.class, Subject_.name, request.getSubjectName(), session);
    if (!subjectOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown subject name").build();
    }

    // Build the Grade entity
    Grade grade = new Grade();
    grade.setSubject(subjectOpt.get());
    grade.setTeacher(teacher);
    grade.setStudent(student);
    grade.setDate(request.getDate());
    grade.setTitle(request.getTitle());
    grade.setGrade(request.getGrade());

    session.persist(grade);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(grade.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}/grades/{grade_id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentTeacherAdministratorSecured
  @ParentOfStudentSecured
  @TeacherOfStudentSecured
  public Response getStudentGradeById(
      @PathParam("grade_id") String gradeId, @Context UriInfo uriInfo) {

    // Fetch grade
    Optional<Grade> gradeOpt =
        DatabaseHandler.fetchEntityBy(Grade.class, Grade_.id, Integer.parseInt(gradeId));
    if (!gradeOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    return Response.ok(gradeOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/grades/{grade_id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response putStudentGradeById(
      PostGradeRequest request,
      @PathParam("id") String studentId,
      @PathParam("grade_id") String gradeId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get teacher who made the request
    User teacher = AuthenticationSession.getCurrentUser(crc, session);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST).build();
    }

    // Fetch student
    User student = session.get(User.class, Integer.parseInt(studentId));
    if (student == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    // Fetch the subject entity by name
    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(
            Subject.class, Subject_.name, request.getSubjectName(), session);
    if (!subjectOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown subject name").build();
    }

    // Fetch Grade entity
    Grade grade = session.get(Grade.class, Integer.parseInt(gradeId));
    if (grade == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    // Check if 'teacher' is the same that created the Grade entity
    if (!teacher.equals(grade.getTeacher())) {
      session.getTransaction().commit();
      session.close();
      return Response.notModified().entity("You cannot modify this grade.").build();
    }

    // Update grade fields
    grade.setSubject(subjectOpt.get());
    grade.setTeacher(teacher);
    grade.setStudent(student);
    grade.setDate(request.getDate());
    grade.setTitle(request.getTitle());
    grade.setGrade(request.getGrade());

    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
  }

  @Path("{id: [0-9]+}/grades/{grade_id: [0-9]+}")
  @DELETE
  @TeacherAdministratorSecured
  @TeacherOfStudentSecured
  public Response deleteStudentGradeById(
      PostGradeRequest request,
      @PathParam("grade_id") String gradeId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch Grade entity
    Grade grade = session.get(Grade.class, Integer.parseInt(gradeId));
    if (grade == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    // Delete Grade
    session.delete(grade);
    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
  }
}

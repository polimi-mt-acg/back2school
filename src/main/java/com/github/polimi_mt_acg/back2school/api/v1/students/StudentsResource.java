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
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get students from DB
    List<User> students =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.STUDENT, session);
    session.getTransaction().commit();
    session.close();

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

    // Check if input user is a student
    if (student.getRole() != Role.STUDENT) {
      return Response.status(Status.BAD_REQUEST).entity("Not a student.").build();
    }

    // Check if a user with same email already exists, if so, do nothing
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    List<User> result =
        dbi.getListSelectFromWhereEqual(User.class, User_.email, student.getEmail(), session);
    if (!result.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT).entity("Student already exists.").build();
    }

    // Otherwise we accept the request. First we fetch Parent entity
    List<User> parentRes =
        dbi.getListSelectFromWhereEqual(User.class, User_.email, parentEmail, session);
    if (parentRes.isEmpty()) {
      // User with parentEmail email not found
      return Response.status(Status.BAD_REQUEST).entity("Unregistered parent email.").build();
    }

    User parent = parentRes.get(0);
    if (parent.getRole() != Role.PARENT) {
      // parentEmail does not belong to a PARENT
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
    List<User> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.id, Integer.parseInt(studentId));

    if (res.isEmpty()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    User student = res.get(0);
    return Response.ok(student, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @ParentOfStudentSecured
  public Response putStudentById(PutStudentRequest newStudent, @PathParam("id") String studentId) {
    // Fetch User
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    List<User> res =
        dbi.getListSelectFromWhereEqual(User.class, User_.id, Integer.parseInt(studentId), session);

    if (res.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    User student = res.get(0);
    updateStudent(student, newStudent);
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
    List<User> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(
                User.class, User_.id, Integer.parseInt(studentId), session);

    if (res.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    User student = res.get(0);

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

    // Fetch User
    Optional<User> studentOpt = fetchStudent(Integer.parseInt(studentId), session);
    if (!studentOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    User student = studentOpt.get();

    // Fetch the subject entity by name
    Optional<Subject> subjectOpt = fetchSubject(request.getSubjectName(), session);
    if (!subjectOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown subject name").build();
    }

    Subject subject = subjectOpt.get();

    // Build the Grade entity
    Grade grade = new Grade();
    grade.setSubject(subject);
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
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch Grades
    List<Grade> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(
                Grade.class, Grade_.id, Integer.parseInt(gradeId), session);

    if (res.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    Grade grade = res.get(0);

    session.getTransaction().commit();
    session.close();
    return Response.ok(grade, MediaType.APPLICATION_JSON_TYPE).build();
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

    // Fetch User
    Optional<User> studentOpt = fetchStudent(Integer.parseInt(studentId), session);
    if (!studentOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown student id").build();
    }

    User student = studentOpt.get();

    // Fetch the subject entity by name
    Optional<Subject> subjectOpt = fetchSubject(request.getSubjectName(), session);
    if (!subjectOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown subject name").build();
    }

    Subject subject = subjectOpt.get();

    // Fetch Grade entity
    Optional<Grade> gradeOpt = fetchGrade(Integer.parseInt(gradeId), session);
    if (!gradeOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    Grade grade = gradeOpt.get();

    // Check if 'teacher' is the same that created the Grade entity
    if (!teacher.equals(grade.getTeacher())) {
      session.getTransaction().commit();
      session.close();
      return Response.notModified().entity("You cannot modify this grade.").build();
    }

    // Update grade fields
    grade.setSubject(subject);
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
    Optional<Grade> gradeOpt = fetchGrade(Integer.parseInt(gradeId), session);
    if (!gradeOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown grade id").build();
    }

    Grade grade = gradeOpt.get();

    // Delete Grade
    session.delete(grade);
    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
  }

  private Optional<User> fetchStudent(int studentId, Session session) {
    List<User> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.id, studentId, session);

    if (res.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(res.get(0));
    }
  }

  private Optional<Subject> fetchSubject(String subjectName, Session session) {
    List<Subject> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Subject.class, Subject_.name, subjectName, session);

    if (res.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(res.get(0));
    }
  }

  private Optional<Grade> fetchGrade(int gradeId, Session session) {
    List<Grade> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Grade.class, Grade_.id, gradeId, session);

    if (res.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(res.get(0));
    }
  }

  private void updateStudent(User entity, PutStudentRequest newStudent) {
    entity.setName(newStudent.getName());
    entity.setSurname(newStudent.getSurname());
    entity.setEmail(newStudent.getEmail());
    entity.setPassword(newStudent.getPassword());
  }
}

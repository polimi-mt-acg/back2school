package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.api.v1.classes.ClassesResource;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResource;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectsResource;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;

/** JAX-RS Resource for teachers entity. */
@Path("teachers")
public class TeacherResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public TeacherResponse getTeachers() {
    List<User> teachers =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER);

    return new TeacherResponse(teachers);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postTeachers(PostTeacherRequest request, @Context UriInfo uriInfo) {
    User teacher = request.getTeacher();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Check if input user is a teacher
    if (teacher.getRole() != User.Role.TEACHER) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Provided user error: not a teacher")
          .build();
    }

    // Check if a user with same email already exists, if so, do nothing
    Optional<User> result =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, teacher.getEmail());
    if (result.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity("An account with this email already exists.")
          .build();
    }

    teacher.prepareToPersist();

    session.persist(teacher);
    session.getTransaction().commit();
    session.close();

    // Now the teacher has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(str(teacher.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherById(@PathParam("id") String id, @Context ContainerRequestContext crc) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);

    // Fetch User
    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, as_int(id));
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }
    User teacher = userOpt.get();

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    return Response.ok(teacher, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{teacherId: [0-9]+}/classes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherClasses(
      @PathParam("teacherId") String teacherId,
      @QueryParam("year") Integer year,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {

    User currentUser = AuthenticationSession.getCurrentUser(crc);

    // Fetch request user
    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, as_int(teacherId));
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }
    User teacher = userOpt.get();

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    Session session = DatabaseHandler.getInstance().getNewSession();

    String queryString =
        "SELECT class.* "
            + "FROM class "
            + "LEFT JOIN lecture "
            + "ON lecture.class_id = class.id "
            + "LEFT JOIN user "
            + "ON lecture.teacher_id = user.id "
            + "WHERE user.id = :teacherId";

    List<Class> classes = new ArrayList<>();
    if (year == null) {
      classes =
          session
              .createNativeQuery(queryString, Class.class)
              .setParameter("teacherId", teacherId)
              .getResultList();
    } else {
      // add the academic_year clause
      queryString += " AND class.academic_year = :year";
      classes =
          session
              .createNativeQuery(queryString, Class.class)
              .setParameter("teacherId", teacherId)
              .setParameter("year", year)
              .getResultList();
    }

    ClassesResponse classesResponse = new ClassesResponse();
    for (Class cls : classes) {
      ClassesResponse.Entity entity = new ClassesResponse.Entity();

      entity.setName(cls.getName());
      entity.setAcademicYear(cls.getAcademicYear());
      entity.setUrlClass(
          uriInfo
              .getBaseUriBuilder()
              .path(ClassesResource.class)
              .path(ClassesResource.class, "getClassById")
              .build(cls.getId())
              .toString());
      entity.setUrlClassStudents(
          uriInfo
              .getBaseUriBuilder()
              .path(ClassesResource.class)
              .path(ClassesResource.class, "getClassStudents")
              .build(cls.getId())
              .toString());
      entity.setUrlClassTimetable(
          uriInfo
              .getBaseUriBuilder()
              .path(TeacherResource.class)
              .path(TeacherResource.class, "getTeacherTimetable")
              .build(teacherId, cls.getId())
              .toString());

      // add the created entity to the response list
      classesResponse.getClasses().add(entity);
    }
    return Response.ok(classesResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{teacherId: [0-9]+}/classes/{classId: [0-9]+}/timetable")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherTimetable(
      @PathParam("teacherId") String teacherId,
      @PathParam("classId") String classId,
      @QueryParam("year") Integer year,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);

    // Fetch request user
    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, as_int(teacherId));
    if (!userOpt.isPresent()) {
      print("User not found");
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }
    User teacher = userOpt.get();

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      print("Not allowed user");
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    String queryString =
        "SELECT lecture.* "
            + "FROM lecture "
            + "LEFT JOIN class "
            + "ON class.id = lecture.class_id "
            + "WHERE lecture.teacher_id = :teacherId";

    List<Lecture> lectures = new ArrayList<>();
    if (year == null) {
      lectures =
          session
              .createNativeQuery(queryString, Lecture.class)
              .setParameter("teacherId", teacherId)
              .getResultList();

    } else {
      // add the academic_year clause
      queryString += " AND class.academic_year = :year";

      lectures =
          session
              .createNativeQuery(queryString, Lecture.class)
              .setParameter("teacherId", teacherId)
              .setParameter("year", year)
              .getResultList();
    }

    TimetableResponse timetableResponse = new TimetableResponse();
    for (Lecture lecture : lectures) {
      TimetableResponse.Entity entity = new TimetableResponse.Entity();

      entity.setDatetimeStart(lecture.getDatetimeStart().toString());
      entity.setDatetimeEnd(lecture.getDatetimeEnd().toString());
      entity.setUrlClassroom(
          uriInfo
              .getBaseUriBuilder()
              .path(ClassroomsResource.class)
              .path(ClassroomsResource.class, "getClassroomById")
              .build(lecture.getClassroom().getId())
              .toString());
      entity.setUrlSubject(
          uriInfo
              .getBaseUriBuilder()
              .path(SubjectsResource.class)
              .path(SubjectsResource.class, "getSubjectById")
              .build(lecture.getSubject().getId())
              .toString());

      // add the created entity to the response list
      timetableResponse.getLectures().add(entity);
    }
    return Response.ok(timetableResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }
}

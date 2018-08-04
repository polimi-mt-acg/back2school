package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.api.v1.classes.ClassesResource;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents_stub.ParentsStubResource;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherSecured;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectsResource;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import java.net.URI;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;

/** JAX-RS Resource for teachers entity. */
@Path("teachers")
public class TeachersResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public TeachersResponse getTeachers() {
    List<User> teachers =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER);

    return new TeachersResponse(teachers);
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

    TeacherClassesResponse teacherClassesResponse = new TeacherClassesResponse();
    for (Class cls : classes) {
      TeacherClassesResponse.Entity entity = new TeacherClassesResponse.Entity();

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
              .path(TeachersResource.class)
              .path(TeachersResource.class, "getTeacherTimetable")
              .build(teacherId, cls.getId())
              .toString());

      // add the created entity to the response list
      teacherClassesResponse.getClasses().add(entity);
    }
    return Response.ok(teacherClassesResponse, MediaType.APPLICATION_JSON_TYPE).build();
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

  @Path("{teacherId: [0-9]+}/appointments")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherAppointments(
      @PathParam("teacherId") String teacherId,
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

    List<Appointment> appointments =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Appointment.class, Appointment_.teacher, teacher);

    TeacherAppointmentsResponse teacherAppointmentsResponse = new TeacherAppointmentsResponse();
    for (Appointment appointment : appointments) {
      TeacherAppointmentsResponse.Entity entity = new TeacherAppointmentsResponse.Entity();

      entity.setId(appointment.getId());
      entity.setDatetimeStart(appointment.getDatetimeStart().toString());
      entity.setDatetimeEnd(appointment.getDatetimeEnd().toString());
      entity.setStatus(str(appointment.getStatus()));
      entity.setUrlParent(
          uriInfo
              .getBaseUriBuilder()
              .path(ParentsStubResource.class)
              .path(ParentsStubResource.class, "getParentById")
              .build(appointment.getTeacher().getId())
              .toString());

      // add the created entity to the response list
      teacherAppointmentsResponse.getAppointments().add(entity);
    }
    return Response.ok(teacherAppointmentsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{teacherId: [0-9]+}/appointments")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherSecured
  public Response postTeacherAppointments(
      AppointmentRequest request,
      @PathParam("teacherId") String teacherId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get teacher who made the request
    User teacher = AuthenticationSession.getCurrentUser(crc, session);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      print("Teacher not found!");
      return Response.status(Status.NOT_FOUND).build();
    }

    if (currentUser.getId() != teacher.getId()) {
      print("Not allowed user");
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    if (request.getParentId() == null) {
      session.getTransaction().commit();
      session.close();
      print("Invalid request, parent_id not set!");
      return Response.status(Status.BAD_REQUEST).build();
    }

    // Fetch parent
    User parent = session.get(User.class, request.getParentId());
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      print("Invalid request, parent not found! Given id: ", request.getParentId());
      return Response.status(Status.NOT_FOUND).entity("Unknown parent id").build();
    }

    // Create new appointment
    Appointment appointment = new Appointment();
    appointment.setTeacher(teacher);
    appointment.setParent(parent);
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
    appointment.setStatus(request.getStatus());

    session.persist(appointment);
    session.getTransaction().commit();
    session.close();

    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(this.getClass())
            .path(this.getClass(), "getTeacherAppointmentById")
            .build(teacherId, appointment.getId());

    return Response.created(uri).build();
  }

  @Path("{teacherId: [0-9]+}/appointments/{appointmentId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherAppointmentById(
      @PathParam("teacherId") String teacherId,
      @PathParam("appointmentId") String appointmentId,
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

    // get appointment from db
    Appointment appointment =
        DatabaseHandler.getInstance().getNewSession().get(Appointment.class, as_int(appointmentId));

    TeacherAppointmentsResponse.Entity entity = new TeacherAppointmentsResponse.Entity();
    entity.setId(appointment.getId());
    entity.setDatetimeStart(appointment.getDatetimeStart().toString());
    entity.setDatetimeEnd(appointment.getDatetimeEnd().toString());
    entity.setStatus(str(appointment.getStatus()));
    entity.setUrlParent(
        uriInfo
            .getBaseUriBuilder()
            .path(ParentsStubResource.class)
            .path(ParentsStubResource.class, "getParentById")
            .build(appointment.getTeacher().getId())
            .toString());

    return Response.ok(entity, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{teacherId: [0-9]+}/appointments/{appointmentId: [0-9]+}")
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherSecured
  public Response putTeacherAppointmentById(
      AppointmentRequest appointmentRequest,
      @PathParam("teacherId") String teacherId,
      @PathParam("appointmentId") String appointmentId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    User currentUser = AuthenticationSession.getCurrentUser(crc);

    // Fetch teacher
    User teacher = session.get(User.class, as_int(teacherId));
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      print("User not found");
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }

    if (currentUser.getId() != teacher.getId()) {
      session.getTransaction().commit();
      session.close();
      print("Not allowed user");
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    // Fetch parent
    User parent = session.get(User.class, appointmentRequest.getParentId());
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      print("Teacher not found");
      return Response.status(Status.BAD_REQUEST).entity("Teacher not found").build();
    }

    // Fetch appointment
    Appointment appointment = session.get(Appointment.class, as_int(appointmentId));
    if (appointment == null) {
      session.getTransaction().commit();
      session.close();
      print("Appointment not found");
      return Response.status(Status.NOT_FOUND).entity("Appointment not found").build();
    }

    appointment.setParent(parent);
    appointment.setDatetimeStart(appointmentRequest.getDatetimeStart());
    appointment.setDatetimeEnd(appointmentRequest.getDatetimeEnd());
    appointment.setStatus(appointmentRequest.getStatus());

    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
  }

  @Path("{teacherId: [0-9]+}/notifications")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherNotifications(
      @PathParam("teacherId") String teacherId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch request user
    User teacher = session.get(User.class, as_int(teacherId));
    if (teacher == null) {
      print("User not found");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      print("Not allowed user");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    // query to get the teacher notifications
    String queryString =
        // query for PERSONAL-TEACHER notifications type
        "SELECT notification.* "
            + "FROM notification "
            + "WHERE notification.type = :notificationType1 "
            + "UNION "
            // query for PERSONAL-TEACHER notifications type
            + "SELECT notification.* "
            + "FROM notification "
            + "LEFT JOIN lecture "
            + "ON lecture.class_id = notification.target_class_id "
            + "LEFT JOIN user "
            + "ON user.id = lecture.teacher_id "
            + "WHERE notification.type = :notificationType2 AND notification.target_user_id = :teacherId "
            + "UNION "
            // query for CLASS-TEACHER notifications type
            + "SELECT notification.* "
            + "FROM notification "
            + "LEFT JOIN lecture "
            + "ON lecture.class_id = notification.target_class_id "
            + "LEFT JOIN user "
            + "ON user.id = lecture.teacher_id "
            + "WHERE notification.type = :notificationType3 AND user.id = :teacherId ";

    List<Notification> notifications;
    notifications =
        session
            .createNativeQuery(queryString, Notification.class)
            .setParameter("notificationType1", "GENERAL-TEACHERS")
            .setParameter("notificationType2", "PERSONAL-TEACHER")
            .setParameter("notificationType3", "CLASS-TEACHER")
            .setParameter("teacherId", teacherId)
            .getResultList();

    TeacherNotificationsResponse teacherNotificationsResponse = new TeacherNotificationsResponse();
    for (Notification notification : notifications) {
      TeacherNotificationsResponse.Entity entity = new TeacherNotificationsResponse.Entity();

      entity.setSubject(notification.getSubject());
      entity.setStatus(notification.getStatusWithRespectTo(teacher));
      entity.setUrl(
          uriInfo
              .getBaseUriBuilder()
              .path(TeachersResource.class)
              .path(TeachersResource.class, "getTeacherNotificationById")
              .build(teacher.getId(), notification.getId()));

      // add the created entity to the response list
      teacherNotificationsResponse.getNotifications().add(entity);
    }
    session.getTransaction().commit();
    session.close();
    return Response.ok(teacherNotificationsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{teacherId: [0-9]+}/notifications/{notificationId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherNotificationById(
      @PathParam("teacherId") Integer teacherId,
      @PathParam("notificationId") Integer notificationId,
      @Context ContainerRequestContext crc) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch teacher
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      print("User not found");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      print("Not allowed user");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    // Fetch notification
    Notification notification = session.get(Notification.class, notificationId);
    if (notification == null) {
      print("Notification not found");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Notification not found").build();
    }

    // mark notification as read if it is the teachers visiting it
    if (currentUser.getRole().equals(Role.TEACHER)) {
      teacher.addNotificationsRead(notification);
    }

    session.getTransaction().commit();
    session.close();

    return Response.ok(notification, MediaType.APPLICATION_JSON).build();
  }
}
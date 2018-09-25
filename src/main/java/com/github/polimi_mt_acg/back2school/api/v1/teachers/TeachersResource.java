package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.classes.ClassesResource;
import com.github.polimi_mt_acg.back2school.api.v1.classes.ClassesResponse;
import com.github.polimi_mt_acg.back2school.api.v1.classrooms.ClassroomsResource;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.parents.ParentsResource;
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
import java.time.LocalDateTime;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.*;

@Path("teachers")
public class TeachersResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getTeachers(@Context UriInfo uriInfo) {
    // Get teachers from DB
    List<User> parents =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.TEACHER);

    // For each user, build a URI to /parents/{id}
    List<URI> uris = new ArrayList<>();
    for (User parent : parents) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(parent.getId())).build();
      uris.add(uri);
    }

    TeachersResponse response = new TeachersResponse();
    response.setTeachers(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postTeachers(User newUser, @Context UriInfo uriInfo) {
    if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("User must have an email address.").build();
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Check if a user with same email already exists, if so, do nothing
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, newUser.getEmail(), session);

    if (userOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity(new StatusResponse(Status.CONFLICT, "A user with this email already exists"))
          .build();
    }
    // force to be a parent since this endpoint meaning
    newUser.setRole(Role.TEACHER);

    newUser.prepareToPersist();
    session.persist(newUser);
    session.getTransaction().commit();
    session.close();

    // Now the teacher has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(str(newUser.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherById(@PathParam("id") Integer teacherId) {
    // Fetch User
    Optional<User> parentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, teacherId);
    if (!parentOpt.isPresent() || !parentOpt.get().getRole().equals(Role.TEACHER)) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    return Response.ok(parentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response putTeacherById(User newTeacher, @PathParam("id") Integer teacherId) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }

    // Update teacher fields
    teacher.setName(newTeacher.getName());
    teacher.setSurname(newTeacher.getSurname());
    teacher.setEmail(newTeacher.getEmail());
    teacher.setNewPassword(newTeacher.getNewPassword());
    teacher.prepareToPersist();

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/classes")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherClasses(
      @PathParam("id") Integer teacherId,
      @QueryParam("year") Integer year,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch request user
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, teacherId, session);
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }
    User teacher = userOpt.get();

    // get all the classes in system
    List<Class> allClasses = dbi.getListSelectFrom(Class.class);
    // get all the lecture of the teacher
    List<Lecture> teacherLectures =
        dbi.getListSelectFromWhereEqual(Lecture.class, Lecture_.teacher, teacher, session);

    // extract which are the teacher classes from his/her lectures
    List<Class> teacherClasses = new ArrayList<>();
    for (Class c : allClasses) {
      for (Lecture l : teacherLectures) {
        if (l.getClass_().getId() == c.getId()) {
          if (year == null) {
            teacherClasses.add(c);
          } else if (c.getAcademicYear() == year) {
            teacherClasses.add(c);
          }
          break;
        }
      }
    }

    ClassesResponse teacherClassesResponse = new ClassesResponse();
    for (Class cls : teacherClasses) {
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

      // add the created entity to the response list
      teacherClassesResponse.getClasses().add(entity);
    }

    session.getTransaction().commit();
    session.close();

    return Response.ok(teacherClassesResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/classes/{classId: [0-9]+}/timetable")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherTimetable(
      @PathParam("id") Integer teacherId,
      @PathParam("classId") Integer classId,
      @QueryParam("year") Integer year,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch request user
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, teacherId, session);
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }
    User teacher = userOpt.get();

    // Fetch request class
    Optional<Class> classOpt =
        DatabaseHandler.fetchEntityBy(Class.class, Class_.id, classId, session);
    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }

    // get all the lecture of the teacher
    List<Lecture> teacherLectures =
        dbi.getListSelectFromWhereEqual(Lecture.class, Lecture_.teacher, teacher, session);

    // filter over the current class
    teacherLectures.removeIf(x -> x.getClass_().getId() != classId);

    // in case of selected year, filter
    if (year != null) {
      teacherLectures.removeIf(x -> x.getClass_().getAcademicYear() != year);
    }

    TimetableResponse timetableResponse = new TimetableResponse();
    for (Lecture lecture : teacherLectures) {
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

  @Path("{id: [0-9]+}/appointments")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherAppointments(
      @PathParam("id") Integer teacherId, @Context UriInfo uriInfo) {
    // Fetch request user
    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, teacherId);
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }
    User teacher = userOpt.get();

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
              .path(ParentsResource.class)
              .path(ParentsResource.class, "getParentById")
              .build(appointment.getTeacher().getId())
              .toString());

      // add the created entity to the response list
      teacherAppointmentsResponse.getAppointments().add(entity);
    }
    return Response.ok(teacherAppointmentsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/appointments")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherSecured
  @SameTeacherSecured
  public Response postTeacherAppointments(
      TeacherAppointmentRequest request,
      @PathParam("id") Integer teacherId,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get teacher who made the request
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }

    // Fetch the parent
    User parent = session.get(User.class, request.getParentId());
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown parent id"))
          .build();
    }

    // To check! Do we need to do these checks?
    // Get appointments of the parent
    List<Appointment> parentAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);

    // Is the parent available in that time slot?
    for (Appointment a : parentAppointments) {
      if ((a.getDatetimeStart().isBefore(request.getDatetimeStart())
              && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
          || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
              && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
              && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
              && a.getDatetimeEnd().isEqual(request.getDatetimeEnd()))) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Parent has already an appointment in selected time slot."))
            .build();
      }
    }

    // Get appointments of the teacher
    List<Appointment> teacherAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.teacher, teacher, session);

    // Is the teacher available in that time slot?
    for (Appointment a : teacherAppointments) {
      if ((a.getDatetimeStart().isBefore(request.getDatetimeStart())
              && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
          || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
              && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
              && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
              && a.getDatetimeEnd().isEqual(request.getDatetimeEnd()))) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Teacher has already an appointment in selected time slot."))
            .build();
      }
    }

    // Build the Appointment entity
    Appointment appointment = new Appointment();
    appointment.setParent(parent);
    appointment.setTeacher(teacher);
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
    // forced requested since created first time
    appointment.setStatus(Appointment.Status.REQUESTED);
    appointment.prepareToPersist();

    session.persist(appointment);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(appointment.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/appointments/{appointmentId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherAppointmentById(
      @PathParam("id") Integer teacherId,
      @PathParam("appointmentId") Integer appointmentId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders);

    // Fetch appointment
    Optional<Appointment> appointmentOpt =
        DatabaseHandler.fetchEntityBy(Appointment.class, Appointment_.id, appointmentId);
    if (!appointmentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown appointment id"))
          .build();
    }

    if (currentUser.getRole().equals(Role.TEACHER)
        && appointmentOpt.get().getTeacher().getId() != teacherId) {
      return Response.status(Status.FORBIDDEN)
          .entity(
              new StatusResponse(Status.FORBIDDEN, "You're not allowed to access this appointment"))
          .build();
    }

    return Response.ok(appointmentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/appointments/{appointmentId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherSecured
  @SameTeacherSecured
  public Response putTeacherAppointmentById(
      TeacherAppointmentRequest request,
      @PathParam("id") Integer teacherId,
      @PathParam("appointmentId") Integer appointmentId) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get teacher who made the request
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }

    // Fetch the parent
    User parent = session.get(User.class, request.getParentId());
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown parent id"))
          .build();
    }

    // Fetch Appointment entity
    Appointment appointment = session.get(Appointment.class, appointmentId);
    if (appointment == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown appointment id"))
          .build();
    }

    // Check if 'teacher' is valid
    if (teacher.getId() != appointment.getTeacher().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(
              new StatusResponse(
                  Status.BAD_REQUEST,
                  "Invalid teacherId. It does not correspond to the previous one"))
          .build();
    }

    // Check if 'parent' is valid
    if (parent.getId() != appointment.getParent().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(
              new StatusResponse(
                  Status.BAD_REQUEST,
                  "Invalid parentId. It does not correspond to the previous one"))
          .build();
    }

    // Checks like in POST! But we don't check over the modified appointment!

    // Get appointments of the parent
    List<Appointment> parentAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);
    // Is the parent available in that time slot?
    for (Appointment a : parentAppointments) {
      if (((a.getDatetimeStart().isBefore(request.getDatetimeStart())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
              || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
                  && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
                  && a.getDatetimeEnd().isEqual(request.getDatetimeEnd())))
          && a.getId() != appointment.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Parent has already an appointment in selected time slot."))
            .build();
      }
    }

    // Get appointments of the teacher
    List<Appointment> teacherAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.teacher, teacher, session);
    // Is the teacher available in that time slot?
    for (Appointment a : teacherAppointments) {
      if (((a.getDatetimeStart().isBefore(request.getDatetimeStart())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
              || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
                  && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
                  && a.getDatetimeEnd().isEqual(request.getDatetimeEnd())))
          && a.getId() != appointment.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT,
                    "Teacher has already an appointment in the selected time slot."))
            .build();
      }
    }

    // Update appointment fields
    // appointment.setUser(parent);  is the the same
    // appointment.setTeacher(teacherOpt.get()); is the same
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
    appointment.setStatus(request.getStatus());

    appointment.prepareToPersist();
    session.getTransaction().commit();
    session.close();

    return Response.ok(Status.OK).entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/notifications")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherNotifications(
      @PathParam("id") Integer teacherId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch teacher
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }

    // Fetch notification for the teacher
    // Three possible notifications type for the teacher:

    // 1:General Teachers
    List<NotificationGeneralTeachers> notificationsGT =
        dbi.getListSelectFrom(NotificationGeneralTeachers.class);

    // 2:Class Teacher
    // Initialized an empty NotificationClassTeacher list
    List<NotificationClassTeacher> notificationsCT = new ArrayList<>();
    List<Class> classes = dbi.getListSelectFrom(Class.class);
    List<Lecture> teacherLectures =
        dbi.getListSelectFromWhereEqual(Lecture.class, Lecture_.teacher, teacher);

    for (Class aClass : classes) {
      for (Lecture lecture : teacherLectures) {
        if (aClass.getId() == lecture.getClass_().getId()) {
          notificationsCT.addAll(
              dbi.getListSelectFromWhereEqual(
                  NotificationClassTeacher.class,
                  NotificationClassParent_.targetClass,
                  aClass,
                  session));
          break;
        }
      }
    }

    // 3:Personal Teacher
    List<NotificationPersonalTeacher> notificationPT =
        dbi.getListSelectFromWhereEqual(
            NotificationPersonalTeacher.class,
            NotificationPersonalTeacher_.targetUser,
            teacher,
            session);

    NotificationsResponse response = new NotificationsResponse();
    response.setNotifications(new ArrayList<>());

    for (NotificationGeneralTeachers ngt : notificationsGT) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(ngt.getId())).build();
      response.getNotifications().add(uri);
    }

    for (NotificationClassTeacher nct : notificationsCT) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(nct.getId())).build();
      response.getNotifications().add(uri);
    }

    for (NotificationPersonalTeacher npt : notificationPT) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(npt.getId())).build();
      response.getNotifications().add(uri);
    }
    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/notifications")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParentNotifications(
      NotificationPersonalTeacher npt,
      @PathParam("id") Integer teacherId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    // Here the admin can POST only a direct notification to this teacher
    User currentAdmin = AuthenticationSession.getCurrentUser(httpHeaders);

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get teacher target of the notification
    User teacher = session.get(User.class, teacherId);
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher id"))
          .build();
    }

    npt.setCreator(currentAdmin);
    npt.setTargetUser(teacher);
    npt.setDatetime(LocalDateTime.now());
    npt.prepareToPersist();

    session.persist(npt);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(npt.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/notifications/{notificationId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  @SameTeacherSecured
  public Response getTeacherNotificationById(
      @PathParam("id") Integer teacherId,
      @PathParam("notificationId") Integer notificationId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders, session);

    // Notification can be of three types: GeneralTeachers, ClassTeacher, PersonalTeacher
    // if none of them will be found by the given id -> error not found

    // 1: GeneralTeachers
    NotificationGeneralTeachers notificationGeneralTeachers =
        session.get(NotificationGeneralTeachers.class, notificationId);
    if (notificationGeneralTeachers != null) {
      // mark as read
      if (currentUser.getRole().equals(Role.TEACHER)) {
        currentUser.addNotificationsRead(notificationGeneralTeachers);
      }
      session.getTransaction().commit();
      session.close();
      return Response.ok(notificationGeneralTeachers, MediaType.APPLICATION_JSON_TYPE).build();
    }

    // 2: ClassTeacher
    NotificationClassTeacher notificationClassTeacher =
        session.get(NotificationClassTeacher.class, notificationId);
    if (notificationClassTeacher != null) {
      // Teacher logged in
      if (currentUser.getRole().equals(Role.TEACHER)) {
        // mark as read
        currentUser.addNotificationsRead(notificationClassTeacher);
      }
      // anyway even if Administrator logged in
      session.getTransaction().commit();
      session.close();
      notificationClassTeacher.setTargetClass(null); // so won't be serialized
      return Response.ok(notificationClassTeacher, MediaType.APPLICATION_JSON_TYPE).build();
    }

    // 3: PersonalTeacher
    NotificationPersonalTeacher notificationPersonalTeacher =
        session.get(NotificationPersonalTeacher.class, notificationId);
    if (notificationPersonalTeacher != null) {
      // mark as read
      if (currentUser.getRole().equals(Role.TEACHER)) {
        currentUser.addNotificationsRead(notificationPersonalTeacher);
      }
      session.getTransaction().commit();
      session.close();
      notificationPersonalTeacher.setTargetUser(null); // so won't be serialized
      return Response.ok(notificationPersonalTeacher, MediaType.APPLICATION_JSON_TYPE).build();
    }

    session.getTransaction().commit();
    session.close();
    return Response.status(Status.NOT_FOUND)
        .entity(
            new StatusResponse(
                Status.NOT_FOUND,
                "No notification matching teacher id and notification id was found"))
        .build();
  }
}

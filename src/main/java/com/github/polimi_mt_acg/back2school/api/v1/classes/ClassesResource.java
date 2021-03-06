package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResource;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentsResource;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

/** JAX-RS Resource for class entity. */
@Path("classes")
public class ClassesResource {

  @Context UriInfo uriInfo;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getClasses(@Context UriInfo uriInfo) {
    // Get classes from DB
    List<Class> classes = DatabaseHandler.getInstance().getListSelectFrom(Class.class);

    ClassesResponse classesResponse = new ClassesResponse();
    for (Class cls : classes) {
      ClassesResponse.Entity entity = new ClassesResponse.Entity();

      entity.setName(cls.getName());
      entity.setAcademicYear(cls.getAcademicYear());
      entity.setUrlClass(
          uriInfo
              .getBaseUriBuilder()
              .path(this.getClass())
              .path(this.getClass(), "getClassById")
              .build(str(cls.getId()))
              .toString());

      classesResponse.getClasses().add(entity);
    }
    return Response.ok(classesResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postClasses(ClassRequest request, @Context UriInfo uriInfo) {
    if (!request.isValidForPost()) return request.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Class aClass = new Class();
    aClass.setName(request.getName());
    aClass.setAcademicYear(request.getAcademicYear());

    for (Integer studentId : request.getStudentsIds()) {
      // get student from db
      User student = session.get(User.class, studentId);
      aClass.addStudent(student);
    }

    session.persist(aClass);
    session.getTransaction().commit();
    session.close();

    // Now class has the ID field filled by Hibernate
    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(this.getClass())
            .path(this.getClass(), "getClassById")
            .build(aClass.getId());
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{classId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassById(@PathParam("classId") Integer classId, @Context UriInfo uriInfo) {
    // Fetch the class
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.id, classId);

    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }
    Class aClass = classOpt.get();

    ClassResponse classResponse = new ClassResponse();
    classResponse.setName(aClass.getName());
    classResponse.setAcademicYear(aClass.getAcademicYear());
    for (User student : aClass.getClassStudents()) {
      ClassResponse.Entity entity = new ClassResponse.Entity();
      entity.setName(student.getName());
      entity.setSurname(student.getSurname());
      entity.setEmail(student.getEmail());
      entity.setUrl(
          uriInfo
              .getBaseUriBuilder()
              .path(StudentsResource.class)
              .path(StudentsResource.class, "getStudentById")
              .build(student.getId()));
      classResponse.getStudents().add(entity);
    }

    return Response.ok(classResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{classId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putClassById(
      ClassRequest request, @PathParam("classId") Integer classId, @Context UriInfo uriInfo) {
    if (!request.isValidForPut(classId)) return request.getInvalidPutResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();
    // Fetch the class
    Class aClass = session.get(Class.class, classId);

    // Update class fields
    aClass.setName(request.getName());
    aClass.setAcademicYear(request.getAcademicYear());
    // reset array of students, so if they're changed it will be updated wi those new
    aClass.setClassStudents(new ArrayList<>());

    for (Integer studentId : request.getStudentsIds()) {
      // get student from db
      User student = session.get(User.class, studentId);
      aClass.addStudent(student);
    }

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{classId: [0-9]+}/students")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassStudents(@PathParam("classId") Integer classId) {
    // Fetch the class
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.id, classId);

    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }
    Class aClass = classOpt.get();

    ClassStudentsResponse classStudentsResponse = new ClassStudentsResponse();
    for (User student : aClass.getClassStudents()) {
      ClassStudentsResponse.Entity entity = new ClassStudentsResponse.Entity();
      entity.setName(student.getName());
      entity.setSurname(student.getSurname());
      entity.setEmail(student.getEmail());
      entity.setUrl(
          uriInfo
              .getBaseUriBuilder()
              .path(StudentsResource.class)
              .path(StudentsResource.class, "getStudentById")
              .build(student.getId()));
      classStudentsResponse.getStudents().add(entity);
    }
    return Response.ok(classStudentsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{classId: [0-9]+}/students")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postClassStudents(
      ClassStudentsRequest request, @PathParam("classId") Integer classId) {
    if (!request.isValidForPost()) return request.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch the class
    Class aClass = session.get(Class.class, classId);
    if (aClass == null) {
      print("Unknown class id: ", classId);
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }

    // Fetch the student
    User newStudent = session.get(User.class, request.getStudentId());
    if (newStudent == null || !newStudent.getRole().equals(User.Role.STUDENT)) {
      print("Unknown student id: ", request.getStudentId());
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown student id"))
          .build();
    }

    // check if the student already belongs to the class
    for (User student : aClass.getClassStudents()) {
      if (student.getId() == newStudent.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(new StatusResponse(Status.CONFLICT, "Student already belongs to class"))
            .build();
      }
    }

    // add the student to the class
    aClass.addStudent(newStudent);

    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{classId: [0-9]+}/students/{studentId: [0-9]+}")
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response deleteClassStudentById(
      @PathParam("classId") Integer classId, @PathParam("studentId") Integer studentId) {

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch the class
    Class aClass = session.get(Class.class, classId);
    if (aClass == null) {
      print("Unknown class id: ", classId);
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }

    // get a list of students without the one removed
    List<User> updatedStudents =
        aClass
            .getClassStudents()
            .stream()
            .filter(x -> x.getId() != studentId)
            .collect(Collectors.toList());

    // if size of updated students list is the same: no student was found for
    // the removed id
    if (updatedStudents.size() == aClass.getClassStudents().size()) {
      print("Student with id: ", studentId, " not belonging to the class.");
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Student not belonging to class"))
          .build();
    }

    // update the students list without the removed one to the class
    aClass.setClassStudents(updatedStudents);
    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{classId: [0-9]+}/notifications/send-to-teachers")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToTeachersOfClass(
      Notification.NotificationRequest request,
      @PathParam("classId") Integer classId,
      @Context HttpHeaders httpHeaders) {
    if (!request.isValidForPost()) return request.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Class aClass = session.get(Class.class, classId);
    if (aClass == null) {
      print("Unknown class id: ", classId);
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(httpHeaders);

    // Create new notification entity from request
    NotificationClassTeacher notificationClassTeacher = new NotificationClassTeacher();
    notificationClassTeacher.setCreator(creator);
    notificationClassTeacher.setSubject(request.getSubject());
    notificationClassTeacher.setText(request.getText());
    // notificationClassTeacher.setDatetime() already now
    notificationClassTeacher.setTargetClass(aClass);

    session.persist(notificationClassTeacher);
    session.getTransaction().commit();
    session.close();

    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(NotificationsResource.class)
            .path(NotificationsResource.class, "getNotificationById")
            .build(notificationClassTeacher.getId());
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{classId: [0-9]+}/notifications/send-to-parents")
  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response sendNotificationToParentsOfClass(
      Notification.NotificationRequest request,
      @PathParam("classId") Integer classId,
      @Context HttpHeaders httpHeaders) {
    if (!request.isValidForPost()) return request.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Class aClass = session.get(Class.class, classId);
    if (aClass == null) {
      print("Unknown class id: ", classId);
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown class id"))
          .build();
    }

    // Get the notification creator
    User creator = AuthenticationSession.getCurrentUser(httpHeaders);

    // Create new notification entity from request
    NotificationClassParent notificationClassParent = new NotificationClassParent();
    notificationClassParent.setCreator(creator);
    notificationClassParent.setSubject(request.getSubject());
    notificationClassParent.setText(request.getText());
    // notificationClassTeacher.setDatetime() already now
    notificationClassParent.setTargetClass(aClass);

    session.persist(notificationClassParent);
    session.getTransaction().commit();
    session.close();

    URI uri =
        uriInfo
            .getBaseUriBuilder()
            .path(NotificationsResource.class)
            .path(NotificationsResource.class, "getNotificationById")
            .build(notificationClassParent.getId());
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.students.StudentsResource;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Class_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.as_int;
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
  @AdministratorSecured
  public Response postClasses(ClassRequest request, @Context UriInfo uriInfo) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Class aClass = new Class();
    aClass.setName(request.getName());
    aClass.setAcademicYear(request.getAcademicYear());
    aClass.setClassStudents(new ArrayList<>());

    for (Integer studentId : request.getStudentsIds()) {
      // get student from db
      User student = session.get(User.class, studentId);
      if (student == null) {
        print("Student with id: ", studentId, " NOT known!");
        return Response.status(Status.BAD_REQUEST)
            .entity("Student with id: " + str(studentId) + " NOT known!")
            .build();
      }
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
    return Response.created(uri).build();
  }

  @Path("{classId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassById(@PathParam("classId") String classId, @Context UriInfo uriInfo) {
    // Fetch the class
    Optional<Class> classOpt =
        DatabaseHandler.fetchEntityBy(Class.class, Class_.id, Integer.parseInt(classId));

    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown class id").build();
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

  @Path("{classId: [0-9]+}/students")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassStudents(@PathParam("classId") String classId) {
    // Fetch the class
    Optional<Class> classOpt =
        DatabaseHandler.fetchEntityBy(Class.class, Class_.id, as_int(classId));

    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown class id").build();
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
}

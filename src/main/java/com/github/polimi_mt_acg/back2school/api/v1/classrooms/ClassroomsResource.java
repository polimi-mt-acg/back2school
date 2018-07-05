package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.Classroom_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/classrooms")
public class ClassroomsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getClassrooms(@Context UriInfo uriInfo) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get classrooms from DB
    List<Classroom> classrooms =
            DatabaseHandler.getInstance()
                    .getListSelectFrom(Classroom.class);
    session.getTransaction().commit();
    session.close();

    // For each classroom, build a URI to /classroom/{id}
    List<URI> uris = new ArrayList<>();
    for (Classroom classroom : classrooms) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(classroom.getId())).build();
      uris.add(uri);
    }

    ClassroomsResponse response = new ClassroomsResponse();
    response.setClassrooms(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postClassrooms(Classroom classroom, @Context UriInfo uriInfo) {

    // Check if a classroom with same name already exists, if so, do nothing
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    List<Classroom> result =
            dbi.getListSelectFromWhereEqual(Classroom.class, Classroom_.name, classroom.getName(), session);
    if (!result.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Response.Status.CONFLICT).entity("Classroom already exists.").build();
    }

    // Otherwise we accept the request.
    session.persist(classroom);
    session.getTransaction().commit(); // Makes classrrom persisted.
    session.close();

    // Now classroom has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(classroom.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassroomID (@PathParam("id") String classroomId){
    // Fetch Classroom
    List<Classroom> res =
            DatabaseHandler.getInstance()
                    .getListSelectFromWhereEqual(Classroom.class, Classroom_.id, Integer.parseInt(classroomId));

    if (res.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).entity("Unknown classroom id").build();
    }

    Classroom classroom = res.get(0);
    return Response.ok(classroom, MediaType.APPLICATION_JSON_TYPE).build();
  }


  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putClassroomById(PutClassroomRequest newClassroom, @PathParam("id") String classroomId) {
    // Fetch Classroom
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    List<Classroom> res =
            dbi.getListSelectFromWhereEqual(Classroom.class, Classroom_.id, Integer.parseInt(classroomId), session);

    if (res.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Response.Status.NOT_FOUND).entity("Unknown classroom id").build();
    }

    Classroom classroom = res.get(0);
    updateClassroom(classroom, newClassroom);
    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().build();
  }

  private void updateClassroom(Classroom classroom, PutClassroomRequest newClassroom) {
    classroom.setName(newClassroom.getName());
    classroom.setFloor(newClassroom.getFloor());
    classroom.setBuilding(newClassroom.getBuilding());

  }
}

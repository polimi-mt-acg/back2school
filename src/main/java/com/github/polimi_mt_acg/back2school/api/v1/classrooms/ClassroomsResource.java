package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
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
    List<Classroom> classrooms = DatabaseHandler.getInstance().getListSelectFrom(Classroom.class);
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
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postClassrooms(Classroom newClassroom, @Context UriInfo uriInfo) {
    if (!newClassroom.isValidForPost()) return newClassroom.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Otherwise we accept the request.
    session.persist(newClassroom);
    session.getTransaction().commit(); // Makes classroom persisted.
    session.close();

    // Now classroom has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(newClassroom.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Response.Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassroomById(@PathParam("id") Integer classroomId) {
    // Fetch Classroom
    List<Classroom> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Classroom.class, Classroom_.id, classroomId);

    if (res.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND)
          .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown classroom id"))
          .build();
    }

    Classroom classroom = res.get(0);
    return Response.ok(classroom, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putClassroomById(Classroom newClassroom, @PathParam("id") Integer classroomId) {
    if (!newClassroom.isValidForPut(classroomId)) return newClassroom.getInvalidPutResponse();

    // Fetch Classroom
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Classroom classroom = session.get(Classroom.class, classroomId);
    classroom.setName(newClassroom.getName());
    classroom.setFloor(newClassroom.getFloor());
    classroom.setBuilding(newClassroom.getBuilding());

    classroom.prepareToPersist();
    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Response.Status.OK)).build();
  }
}

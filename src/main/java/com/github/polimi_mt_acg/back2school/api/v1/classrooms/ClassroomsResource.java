package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
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
  public Response postClassrooms(Classroom classroom, @Context HttpHeaders hh) {

    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();
    session.persist(classroom);
    session.getTransaction().commit();
    session.close();

    return Response.ok(classroom, MediaType.APPLICATION_JSON).build();
  }

  @Path("{id}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public void getClassroomID (@PathParam("id") int id){
    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();
    List<Classroom> classrooms =
            DatabaseHandler.getInstance()
                    .getListSelectFromWhereEqual(Classroom.class, Classroom_.id, id, session);

    session.close();
//    return new ClassroomsResponse(classrooms);
  }


}

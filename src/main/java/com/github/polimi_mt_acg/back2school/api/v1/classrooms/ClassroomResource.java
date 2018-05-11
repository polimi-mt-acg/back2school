package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectResponse;
import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.Classroom_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/classrooms")
public class ClassroomResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public ClassroomResponse getClassrooms() throws JsonProcessingException {
    List<Classroom> classrooms = DatabaseHandler.getInstance().getListSelectFrom(Classroom.class);

    return new ClassroomResponse(classrooms);
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
  public ClassroomResponse getClassroomID (@PathParam("id") int id){
    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();
    List<Classroom> classrooms =
            DatabaseHandler.getInstance()
                    .getListSelectFromWhereEqual(Classroom.class, Classroom_.id, id, session);

    session.close();
    return new ClassroomResponse(classrooms);
  }

}

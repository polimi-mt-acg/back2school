package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("subjects")
public class SubjectResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public SubjectResponse getSubjects() throws JsonProcessingException {
    List<Subject> subjects = DatabaseHandler.getInstance().getListSelectFrom(Subject.class);

    return new SubjectResponse(subjects);
  }

  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postSubjects(Subject subject, @Context HttpHeaders hh) {

    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();
    session.persist(subject);
    session.getTransaction().commit();
    session.close();

    return Response.ok(subject, MediaType.APPLICATION_JSON).build();
  }

  @Path("{id}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public SubjectResponse getSubjectID(@PathParam("id") int id) {
    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();
    List<Subject> subjects =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Subject.class, Subject_.id, id, session);

    session.close();
    return new SubjectResponse(subjects);
  }

  @Path("{id}")
  @PUT
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putSubjectID(@PathParam("id") int id, Subject subject) {

    Subject updateSubj = new Subject();
    DatabaseHandler dhi = DatabaseHandler.getInstance();
    Session session = dhi.getNewSession();
    session.beginTransaction();


    session.persist(updateSubj);
    session.getTransaction().commit();
    session.close();

    return Response.ok(subject, MediaType.APPLICATION_JSON).build();
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.PutSubjectRequest;
import com.github.polimi_mt_acg.back2school.api.v1.subjects.SubjectsResponse;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.Subject_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Path("/subjects")
public class SubjectsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getSubjects(@Context UriInfo uriInfo) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get subject from DB
    List<Subject> subjects = DatabaseHandler.getInstance().getListSelectFrom(Subject.class);
    session.getTransaction().commit();
    session.close();

    // For each subject, build a URI to /subject/{id}
    List<URI> uris = new ArrayList<>();
    for (Subject subject : subjects) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(subject.getId())).build();
      uris.add(uri);
    }

    SubjectsResponse response = new SubjectsResponse();
    response.setSubjects(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postSubjects(Subject subject, @Context UriInfo uriInfo) {

    // Check if a subject with same name already exists, if so, do nothing
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    List<Subject> result =
        dbi.getListSelectFromWhereEqual(Subject.class, Subject_.name, subject.getName(), session);
    if (!result.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Response.Status.CONFLICT).entity("Subject already exists.").build();
    }

    // Otherwise we accept the request.
    session.persist(subject);
    session.getTransaction().commit(); // Makes subject persisted.
    session.close();

    // Now subject has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(subject.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getSubjectById(@PathParam("id") String subjectId) {
    // Fetch Subject
    List<Subject> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(Subject.class, Subject_.id, Integer.parseInt(subjectId));

    if (res.isEmpty()) {
      return Response.status(Response.Status.NOT_FOUND).entity("Unknown subject id").build();
    }

    Subject subject = res.get(0);
    return Response.ok(subject, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putSubjectById(PutSubjectRequest newSubject, @PathParam("id") String subjectId) {
    // Fetch Subject
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    List<Subject> res =
        dbi.getListSelectFromWhereEqual(
            Subject.class, Subject_.id, Integer.parseInt(subjectId), session);

    if (res.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Response.Status.NOT_FOUND).entity("Unknown subject id").build();
    }

    Subject subject = res.get(0);
    updateSubject(subject, newSubject);
    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().build();
  }

  private void updateSubject(Subject subject, PutSubjectRequest newSubject) {
    subject.setName(newSubject.getName());
    subject.setDescription(newSubject.getDescription());
  }
}

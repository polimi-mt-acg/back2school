package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.model.Subject_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Path("/subjects")
public class SubjectsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getSubjects(@Context UriInfo uriInfo) {
    // Get subjects from DB
    List<Subject> subjects = DatabaseHandler.getInstance().getListSelectFrom(Subject.class);

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
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postSubjects(Subject newSubject, @Context UriInfo uriInfo) {
    if (!newSubject.isValidForPost()) return newSubject.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    newSubject.prepareToPersist();
    session.persist(newSubject);
    session.getTransaction().commit();
    session.close();

    // Now subject has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(newSubject.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getSubjectById(@PathParam("id") Integer subjectId) {
    // Fetch Subject
    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(Subject.class, Subject_.id, subjectId);

    if (!subjectOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown subject id"))
          .build();
    }

    return Response.ok(subjectOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putSubjectById(Subject newSubject, @PathParam("id") Integer subjectId) {
    if (!newSubject.isValidForPut(subjectId)) return newSubject.getInvalidPutResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Subject subject = session.get(Subject.class, subjectId);
    subject.setName(newSubject.getName());
    subject.setDescription(newSubject.getDescription());

    subject.prepareToPersist();
    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }
}

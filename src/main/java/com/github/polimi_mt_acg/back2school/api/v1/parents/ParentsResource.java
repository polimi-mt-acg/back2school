package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentTeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javafx.scene.Parent;
import org.hibernate.Session;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

@Path("parents")
public class ParentsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getParents(@Context UriInfo uriInfo) {
    // Get parents from DB
    List<User> parents =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.PARENT);

    // For each user, build a URI to /parents/{id}
    List<URI> uris = new ArrayList<>();
    for (User parent : parents) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(parent.getId())).build();
      uris.add(uri);
    }

    ParentsResponse response = new ParentsResponse();
    response.setParents(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParents(PostParentRequest request, @Context UriInfo uriInfo) {
    User parent = request.getParent();
    String studentEmail = request.getStudentEmail();

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Check if input user is a parent
    if (parent.getRole() != Role.PARENT) {
      return Response.status(Status.BAD_REQUEST).entity("Not a parent.").build();
    }

    // Check if a user with same email already exists, if so, do nothing
    List<User> result =
        dbi.getListSelectFromWhereEqual(User.class, User_.email, parent.getEmail(), session);
    if (!result.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT).entity("Parent already exists.").build();
    }

    // Otherwise we accept the request. First we fetch Student entity
    List<User> studentRes =
        dbi.getListSelectFromWhereEqual(User.class, User_.email, studentEmail, session);
    if (studentRes.isEmpty()) {
      // User with studentEmail email not found
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST).entity("Unregistered student email.").build();
    }

    User student = studentRes.get(0);
    if (student.getRole() != Role.STUDENT) {
      // studentEmail does not belong to a STUDENT
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST).entity("Not a student.").build();
    }

    parent.addChild(student);
    parent.prepareToPersist();

    session.persist(parent);

    session.getTransaction().commit(); // Makes parent persisted.
    session.close();

    // Now parent has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(parent.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentById(
          @PathParam("id") String parentId) {
    // Fetch User
    Optional<User> parentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, Integer.parseInt(parentId));
    if (!parentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown parent id").build();
    }
    User parent = parentOpt.get();

    return Response.ok(parent, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response putParentById(PutParentRequest newParent,
                                @PathParam("id") String parentId) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity("Unknown parent id").build();
    }



    // Update student fields
    parent.setName(newParent.getName());
    parent.setSurname(newParent.getSurname());
    parent.setEmail(newParent.getEmail());
    parent.setPassword(newParent.getPassword());

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().build();
  }

  @Path("{id: [0-9]+}/children")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentChildren(@PathParam("id") String parentId) {

  // Fetch User
    Optional<User> parentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, Integer.parseInt(parentId));
    if (!parentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown parent id").build();
    }
    User parent = parentOpt.get();

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();


    for(User c : parent.getChildren()){
      // Check user with child email
      List<User> child =
              dbi.getListSelectFromWhereEqual(User.class, User_.email, c.getEmail(), session);
      if (!child.isEmpty()) {

        session.getTransaction().commit();
        session.close();
      }

    }



    ParentChildrenResponse response = new ParentChildrenResponse();


    response.setChildren(parent.getChildren());



    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }





}

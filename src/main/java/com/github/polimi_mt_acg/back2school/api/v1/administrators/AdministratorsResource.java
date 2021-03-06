package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

@Path("administrators")
public class AdministratorsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getAdministrators(@Context UriInfo uriInfo) {
    // Get administrators from DB
    List<User> administrators =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.ADMINISTRATOR);

    // For each user, build a URI to /administrators/{id}
    List<URI> uris = new ArrayList<>();
    for (User administrator : administrators) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(administrator.getId())).build();
      uris.add(uri);
    }

    AdministratorsResponse response = new AdministratorsResponse();
    response.setAdministrators(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postAdministrators(User newUser, @Context UriInfo uriInfo) {
    if (!newUser.isValidForPost()) return newUser.getInvalidPostResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // force to be an administrator since this endpoint meaning
    newUser.setRole(Role.ADMINISTRATOR);
    newUser.prepareToPersist();

    session.persist(newUser);
    session.getTransaction().commit();
    session.close();

    // Now the teacher has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(str(newUser.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{administratorId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getAdministratorById(@PathParam("administratorId") Integer administratorId) {
    // Fetch User
    Optional<User> administratorOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, administratorId);
    if (!administratorOpt.isPresent()
        || !administratorOpt.get().getRole().equals(Role.ADMINISTRATOR)) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown administrator id"))
          .build();
    }
    return Response.ok(administratorOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{administratorId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response putAdministratorById(
      User newUser, @PathParam("administratorId") Integer administratorId) {
    if (!newUser.isValidForPut(administratorId)) return newUser.getInvalidPutResponse();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User administrator = session.get(User.class, administratorId);

    // Update administrator fields
    administrator.setName(newUser.getName());
    administrator.setSurname(newUser.getSurname());
    administrator.setEmail(newUser.getEmail());
    administrator.setPassword(newUser.getNewPassword());

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }
}

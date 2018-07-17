package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.Class_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JAX-RS Resource for class entity. */
@Path("classes")
public class ClassResource {

  @Context UriInfo uriInfo;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getClasses() {
    // Get classes from DB
    List<Class> classes = DatabaseHandler.getInstance().getListSelectFrom(Class.class);

    UriBuilder builder = uriInfo.getBaseUriBuilder();

    // For each user, build a URI to /students/{id}
    List<URI> uris = new ArrayList<>();
    for (Class cls : classes) {
      URI uri = builder.path(ClassResource.class).path(String.valueOf(cls.getId())).build();
      uris.add(uri);
    }

    ClassResponse response = new ClassResponse();
    response.setClasses(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{classId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getClassById(@PathParam("classId") String classId) {
    // Fetch the class
    Optional<Class> classOpt =
        DatabaseHandler.fetchEntityBy(Class.class, Class_.id, Integer.parseInt(classId));

    if (!classOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Unknown class id").build();
    }

    return Response.ok(classOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }
}

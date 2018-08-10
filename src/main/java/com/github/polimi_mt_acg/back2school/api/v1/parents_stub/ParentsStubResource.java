package com.github.polimi_mt_acg.back2school.api.v1.parents_stub;


import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentAdministratorSecured;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * TODO implement real parents class and delete this one
 */
@Path("/parents")
public class ParentsStubResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getParents(@Context UriInfo uriInfo) {
    return Response.ok().build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  public Response getParentById(@PathParam("id") String subjectId) {
    return Response.ok().build();
  }
}

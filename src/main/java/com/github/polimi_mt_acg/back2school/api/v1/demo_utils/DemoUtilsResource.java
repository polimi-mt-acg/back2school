package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

/**
 * JAX-RS Resource for teachers entity.
 */
@Path("demo-utils")
public class DemoUtilsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public DemoUtilsResponse getDemoUtils(@Context UriInfo uriInfo) {

    DemoUtilsResponse demoUtilsResponse = new DemoUtilsResponse();
    demoUtilsResponse.setUrlDeployDemoData(
        uriInfo
            .getBaseUriBuilder()
            .path(this.getClass())
            .path(this.getClass(), "getDeployDemoData")
            .build()
    );

    demoUtilsResponse.setUrlEmptyDatabase(
        uriInfo
            .getBaseUriBuilder()
            .path(this.getClass())
            .path(this.getClass(), "getEmptyDatabase")
            .build()
    );

    return demoUtilsResponse;
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("deploy-demo-data")
  public Response getDeployDemoData(@Context UriInfo uriInfo) {
    return Response.ok("OK").build();
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("empty-database")
  public Response getEmptyDatabase() {
    return Response.ok("OK").build();
  }

}

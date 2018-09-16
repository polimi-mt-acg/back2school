package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Duration;
import java.time.Instant;

/**
 * JAX-RS Resource for teachers entity.
 */
@Path("demo-utils")
public class DemoUtilsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getDemoUtils(@Context UriInfo uriInfo) {

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

    return Response.ok(demoUtilsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("deploy-demo-data")
  public Response getDeployDemoData(@Context UriInfo uriInfo) {
    Instant start = Instant.now();
    DatabaseSeeder.deployScenario("demo_data_scenario");
    Instant end = Instant.now();

    DeployDemoDataResponse deployDemoDataResponse =
        new DeployDemoDataResponse("SUCCESS", Duration.between(start, end).toString());
    return Response.ok(deployDemoDataResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }


  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("empty-database")
  public Response getEmptyDatabase() {
    return Response.ok("OK").build();
  }

}

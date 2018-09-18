package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.time.Duration;
import java.time.Instant;

/** JAX-RS Resource for teachers entity. */
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
            .build());

    demoUtilsResponse.setUrlEmptyDatabase(
        uriInfo
            .getBaseUriBuilder()
            .path(this.getClass())
            .path(this.getClass(), "getEmptyDatabase")
            .build());

    return Response.ok(demoUtilsResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("deploy-demo-data")
  public Response getDeployDemoData(@Context UriInfo uriInfo) {
    DemoUtilsActionResponse demoUtilsActionResponse = new DemoUtilsActionResponse();

    Instant start = Instant.now();
    DatabaseHandler.getInstance().truncateDatabase();
    demoUtilsActionResponse.addAction("TRUNCATE_DATABASE");

    DatabaseSeeder.ensureAdminUserPresent();
    demoUtilsActionResponse.addAction("ENSURE_ADMIN_PRESENT");

    DatabaseSeeder.deployScenario("demo_data_scenario");
    demoUtilsActionResponse.addAction("DEPLOY_DATABASE_SCENARIO(demo_data_scenario)");
    Instant end = Instant.now();

    demoUtilsActionResponse.setStatus("SUCCESS");
    demoUtilsActionResponse.setDuration(Duration.between(start, end).toString());

    return Response.ok(demoUtilsActionResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  @Path("empty-database")
  public Response getEmptyDatabase() {
    DemoUtilsActionResponse demoUtilsActionResponse = new DemoUtilsActionResponse();

    Instant start = Instant.now();
    DatabaseHandler.getInstance().truncateDatabase();
    demoUtilsActionResponse.addAction("TRUNCATE_DATABASE");

    DatabaseSeeder.ensureAdminUserPresent();
    demoUtilsActionResponse.addAction("ENSURE_ADMIN_PRESENT");
    Instant end = Instant.now();

    demoUtilsActionResponse.setStatus("SUCCESS");
    demoUtilsActionResponse.setDuration(Duration.between(start, end).toString());

    return Response.ok(demoUtilsActionResponse, MediaType.APPLICATION_JSON_TYPE).build();
  }
}

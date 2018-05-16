package com.github.polimi_mt_acg.back2school.utils.rest;

import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class HTTPServerManager {

  public static HttpServer startServer(Class resourceClassName, String... packages) {

    // Create a resource config that scans for JAX-RS resources
    final ResourceConfig rc =
        new ResourceConfig()
            .register(resourceClassName)
            .packages(packages)
            .register(JacksonCustomMapper.class)
            .register(JacksonFeature.class);

    // create and start a new instance of grizzly http server
    // exposing the Jersey application at BASE_URI
    return GrizzlyHttpServerFactory.createHttpServer(URI.create(RestFactory.BASE_URI), rc);
  }
}

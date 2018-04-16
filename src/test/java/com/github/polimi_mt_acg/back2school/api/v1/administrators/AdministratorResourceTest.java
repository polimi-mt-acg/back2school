package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.api.v1.JacksonCustomMapper;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

public class AdministratorResourceTest {
    private static final String BASE_URI = "http://localhost:8080/v1/";
    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        server = startServer();

        Client client = ClientBuilder.newClient();
        target = client.target(URI.create(BASE_URI));
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void getAdministrators() throws IOException {
        String responseMsg = target.path("administrators").request(MediaType.APPLICATION_JSON).get(String.class);

        System.out.println(responseMsg);

        assertEquals(1, 1);
    }

    private HttpServer startServer() {
        // Create a resource config that scans for JAX-RS resources and providers
        // in com.github.polimi_mt_acg.back2school.api.v1.administrators.resources package
        final ResourceConfig rc = new ResourceConfig()
                .packages("com.github.polimi_mt_acg.back2school.api.v1.administrators")
                .register(JacksonCustomMapper.class)
                .register(JacksonFeature.class);

        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
}
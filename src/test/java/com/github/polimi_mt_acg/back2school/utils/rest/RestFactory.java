package com.github.polimi_mt_acg.back2school.utils.rest;

import com.github.polimi_mt_acg.back2school.api.v1.Credentials;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.jackson.JacksonFeature;

public class RestFactory {

    public static final String BASE_URI = "http://localhost:8080/v1/";

    public static String authenticate(String email, String password) {
        Client client = buildClient();
        WebTarget target = client.target(URI.create(BASE_URI)).path("auth");
        return target
            .request(MediaType.APPLICATION_JSON)
            .buildPost(Entity.json(new Credentials(email, password)))
            .invoke(String.class);
    }

    public static WebTarget buildWebTarget() {
        Client client = buildClient();
        return client.target(URI.create(BASE_URI));
    }

    private static Client buildClient() {
        return ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    }
}

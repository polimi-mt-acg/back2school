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

  /** The base URI of all the REST APIs */
  public static final String BASE_URI = "http://localhost:8080/v1/";

  /**
   * A convenient method to create a client requesting to /auth endpoint for a session token.
   *
   * @param email The user login email.
   * @param password The user login password.
   * @return The session token if {@code email} and {@code password} are valid.
   * @throws javax.ws.rs.ForbiddenException if {@code email} and {@code password} are not valid.
   */
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

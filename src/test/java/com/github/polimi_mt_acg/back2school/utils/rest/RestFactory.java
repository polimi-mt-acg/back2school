package com.github.polimi_mt_acg.back2school.utils.rest;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.polimi_mt_acg.back2school.api.v1.auth.LoginRequest;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.jackson.JacksonFeature;

public class RestFactory {

  /** The base URI of all the REST APIs */
  public static final String BASE_URI = "http://localhost:8080/api/v1/";
  private static ObjectMapper mapper = null;

  /**
   * A convenient method to create a client requesting the login to the
   * endpoint /auth/login getting back session token.
   *
   * @param email The user login email.
   * @param password The user login password.
   * @return The session token if {@code email} and {@code password} are valid.
   * @throws javax.ws.rs.ForbiddenException if {@code email} and {@code password} are not valid.
   */
  public static String doLoginGetToken(String email, String password) {
    Client client = buildClient();
    WebTarget target = client.target(URI.create(BASE_URI)).path("auth").path("login");

    return target
        .request(MediaType.APPLICATION_JSON)
        .buildPost(Entity.json(new LoginRequest(email, password)))
        .invoke(String.class);
  }

  public static WebTarget buildWebTarget() {
    Client client = buildClient();
    return client.target(URI.create(BASE_URI));
  }

  public static ObjectMapper objectMapper() {
    if (mapper == null) {
      mapper = new ObjectMapper();
      // Handles polymorphism through inheritance
      // mapper.enableDefaultTyping(DefaultTyping.NON_FINAL);
      mapper.registerModule(new JavaTimeModule());
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      mapper.enable(Feature.ALLOW_COMMENTS);
    }
    return mapper;
  }

  private static Client buildClient() {
    return ClientBuilder.newBuilder()
        .register(JacksonFeature.class)
        .register(JacksonCustomMapper.class)
        .build();
  }
}

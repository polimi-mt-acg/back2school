package com.github.polimi_mt_acg.back2school.utils.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.polimi_mt_acg.back2school.api.v1.auth.LoginRequest;
import com.github.polimi_mt_acg.back2school.api.v1.auth.LoginResponse;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;

import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;

public class RestFactory {

  /** The base URI of all the REST APIs */
  public static final String BASE_URI = "http://localhost:8080/api/v1/";

  private static ObjectMapper mapper = null;

  /**
   * A convenient method to create a client requesting the login to the endpoint /auth/login getting
   * back session token.
   *
   * @param email The user login email.
   * @param password The user login password.
   * @return The session token if {@code email} and {@code password} are valid.
   * @throws javax.ws.rs.ForbiddenException if {@code email} and {@code password} are not valid.
   */
  public static String doLoginGetToken(String email, String password) {
    Client client = buildClient();
    WebTarget target = client.target(URI.create(BASE_URI)).path("auth").path("login");

    LoginResponse response =
        target
            .request(MediaType.APPLICATION_JSON)
            .buildPost(Entity.json(new LoginRequest(email, password)))
            .invoke(LoginResponse.class);

    assertNotNull(response);
    assertNotNull(response.token);

    return response.token;
  }

  /**
   * Build a WebTarget that starts from the base URI.
   *
   * @return the target.
   */
  public static WebTarget buildWebTarget() {
    Client client = buildClient();
    return client.target(URI.create(BASE_URI));
  }

  public static WebTarget buildWebTarget(URI uri) {
    Client client = buildClient();
    return client.target(uri);
  }

  /**
   * Authenticate the given target with the provided user and return an invocation builder.
   *
   * @param user The user to be authenticated.
   * @return
   */
  public static Invocation.Builder getAuthenticatedInvocationBuilder(User user, String... path) {
    // Build the Client
    WebTarget target = RestFactory.buildWebTarget();

    // Set target to /notifications
    for (String p : path) {
      target = target.path(p);
    }

    return getAuthenticatedInvocationBuilder(user, target);
  }

  /**
   * Authenticate the given target with the provided user and return an invocation builder.
   *
   * @param user The user to be authenticated.
   * @return
   */
  public static Invocation.Builder getAuthenticatedInvocationBuilder(User user, WebTarget target) {
    // Authenticate
    String token = RestFactory.doLoginGetToken(user.getEmail(), user.getNewPassword());
    assertNotNull(token);
    assertTrue(!token.isEmpty());

    return target.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token);
  }

  public static Invocation.Builder getAuthenticatedInvocationBuilder(User user, URI uri) {
    // Authenticate
    String token = RestFactory.doLoginGetToken(user.getEmail(), user.getNewPassword());
    assertNotNull(token);
    assertTrue(!token.isEmpty());

    WebTarget target = RestFactory.buildWebTarget(uri);

    return target.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, token);
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

  private static String getEndpointFromChunks(Object[] endpointChunks) {
    StringBuilder endpoint = new StringBuilder("/");
    for(Object c: endpointChunks){
      endpoint.append(String.valueOf(c));
      endpoint.append("/");
    }
    endpoint.deleteCharAt(endpoint.length()-1);
    return endpoint.toString();
  }

  /**
   * A convenient method to perform a one-line get request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param endpointChunks The endpoint parts to issue the request at.
   * @return URI of the create resource.
   */
  public static Response doGetRequest(User userForLogin, Object... endpointChunks) {
    String endpoint = getEndpointFromChunks(endpointChunks);

    // Make a GET request
    Invocation get =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, endpoint).buildGet();

    Response response = get.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    return response;
  }

  /**
   * A convenient method to perform a one-line get request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param uri The URI to issue the request at.
   * @return URI of the create resource.
   */
  public static Response doGetRequest(User userForLogin, URI uri) {
    // Make a GET request
    Invocation get =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, uri).buildGet();

    Response response = get.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    return response;
  }

  /**
   * A convenient method to perform a one-line post request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param requestEntity The entity to be sent.
   * @param endpointChunks The endpoint parts to issue the request at.
   * @return URI of the create resource.
   */
  public static URI doPostRequest(User userForLogin, Object requestEntity, Object... endpointChunks) {
    String endpoint = getEndpointFromChunks(endpointChunks);

    // Make a POST request
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, endpoint)
            .buildPost(Entity.json(requestEntity));

    Response response = post.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  /**
   * A convenient method to perform a one-line put request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param requestEntity The entity to be sent.
   * @param endpointChunks The endpoint parts to issue the request at.
   */
  public static void doPutRequest(User userForLogin, Object requestEntity,  Object... endpointChunks) {
    String endpoint = getEndpointFromChunks(endpointChunks);

    // Make a PUT request
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, endpoint)
            .buildPut(Entity.json(requestEntity));

    Response response = post.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  /**
   * A convenient method to perform a one-line put request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param requestEntity The entity to be sent.
   * @param uri The URI to issue the request at.
   */
  public static void doPutRequest(User userForLogin, Object requestEntity,  URI uri) {

    // Make a PUT request
    Invocation post =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, uri)
            .buildPut(Entity.json(requestEntity));

    Response response = post.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }


  /**
   * A convenient method to perform a one-line delete request.
   *
   * @param userForLogin The user to be used for authentication.
   * @param endpointChunks The endpoint parts to issue the request at.
   */
  public static void doDeleteRequest(User userForLogin, Object... endpointChunks) {
    String endpoint = getEndpointFromChunks(endpointChunks);

    // Make a DELETE request
    Invocation delete =
        RestFactory.getAuthenticatedInvocationBuilder(userForLogin, endpoint)
            .buildDelete();

    Response response = delete.invoke();
    assertNotNull(response);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
}

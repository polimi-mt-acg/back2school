package com.github.polimi_mt_acg.back2school.api.v1.auth;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;

/**
 * JAX-RS resource for HTTP client authentication. Check authenticateUser() for additional details
 * on this endpoint.
 */
@Path("auth")
public class AuthenticationResource {

  /**
   * Replies to a HTTP POST to /auth/login endpoint.
   *
   * @param request The client Credentials, i.e. email and password
   * @return The session token is the user is authenticated. A FORBIDDEN Error Response otherwise.
   */
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @POST
  @Path("login")
  public Response userLogin(LoginRequest request) {
    // Get the "authenticated version" of the user using the credentials provided
    User user = getAuthenticatedUser(request.email, request.password);

    // if the user is null: invalid credentials or user does not exist
    if (user == null)
      return Response.status(Response.Status.BAD_REQUEST).build();


    // create a new AuthenticationSession and return back the token to the client
    AuthenticationSession authSession =
        AuthenticationSession.startNewAuthenticationSession(user);


    String token = authSession.getToken();

    return Response.ok(new LoginResponse("authenticated", token)).build();
  }


  @GET
  @Path("logout")
  @Produces(MediaType.APPLICATION_JSON)
  public Response userLogout(@Context HttpHeaders httpHeaders) {
    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders);

    // if the user is null because session expired or other reasons
    if (currentUser == null)
      return Response.status(Response.Status.BAD_REQUEST).build();

    Session session = DatabaseHandler.getInstance().getNewSession();
    // invalidate all the still valid user sessions
    AuthenticationSession.invalidateAllAuthenticationSession(currentUser, session);
    session.close();

    return Response.ok(new LogoutResponse()).build();
  }


  private User getAuthenticatedUser(String email, String password) {
    // Authenticate against the database
    List<User> users =
        DatabaseHandler.getInstance().getListSelectFromWhereEqual(User.class, User_.email, email);

    // User not found
    if (users.isEmpty()) return null;

    User user = users.get(0);

    // Check password validity
    if (!user.passwordEqualsTo(password)) return null;

    // if reached here: the user is valid and can be authenticated
    return user;
  }
}


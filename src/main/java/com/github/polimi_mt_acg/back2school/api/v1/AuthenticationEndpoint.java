package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.Session;

/**
 * JAX-RS resource for HTTP client authentication. Check authenticateUser() for additional details
 * on this endpoint.
 */
@Path("auth")
public class AuthenticationEndpoint {

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response authenticateUser(Credentials request) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    System.out.println("Asking for " + request.email + " " + request.password);
    // Authenticate the user using the credentials provided
    Optional<User> user = authenticate(request.email, request.password, session);

    if (user.isPresent()) {
      // Issue a token for the user
      String token = issueToken(user.get(), session);

      // Return the token on the response
      session.close();
      return Response.ok(token).build();
    } else {
      // No User with provided credentials was found. Return Forbidden error.
      session.close();
      return Response.status(Response.Status.FORBIDDEN).build();
    }
  }

  private Optional<User> authenticate(String email, String password, Session session) {
    // Authenticate against the database. Check if a user with email and password exists
    CriteriaBuilder builder = session.getCriteriaBuilder();
    CriteriaQuery<User> criteria = builder.createQuery(User.class);

    // Select u from User where u.email = email and u.password = password
    Root<User> root = criteria.from(User.class);
    criteria.select(root);
    criteria.where(builder.equal(root.get(User_.email), email));

    session.beginTransaction();
    List<User> results = session.createQuery(criteria).getResultList();
    session.getTransaction().commit();

    if (results.size() == 0) {
      // If no user was found, return an empty optional
      return Optional.empty();
    } else {
      // Check User's password
      User user = results.get(0);
      if (user.passwordEqualsTo(password)) {
        // Return the User to authenticate
        return Optional.of(user);
      } else {
        // Password is not matching.
        return Optional.empty();
      }
    }
  }

  private String issueToken(User user, Session session) {
    // Check if a token is already available for user and is still valid.
    // If so, update last interaction and return the token
    session.beginTransaction();
    List<AuthenticationSession> results =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(
                AuthenticationSession.class, AuthenticationSession_.user, user, session);

    String token;
    if (results.size() > 0) {
      AuthenticationSession authSession = results.get(0);
      if (!authSession.isCancelled()) {
        authSession.setDatetimeLastInteraction(LocalDateTime.now());
        token = authSession.getToken();
      } else {
        token = issueNewToken(user, session);
      }
    } else {
      // If no token is available issue a new one
      token = issueNewToken(user, session);
    }
    session.getTransaction().commit();
    // Return the issued token
    return token;
  }

  private String issueNewToken(User user, Session session) {
    // Create a new AuthenticationSession, make it persistent
    AuthenticationSession authSession = new AuthenticationSession();
    authSession.setUser(user);
    session.persist(authSession);

    // Return the newly created token
    return authSession.getToken();
  }
}

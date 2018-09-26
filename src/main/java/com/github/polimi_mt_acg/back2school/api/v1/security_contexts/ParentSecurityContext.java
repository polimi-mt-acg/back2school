package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * AdministratorSecurityContext implements a request filter for JAX-RS REST APIs. It implements a
 * "Administrators-only" security policy. A REST API that is annotated with @AdministratorSecured
 * can only be accessed if: a) the client is authenticated. b) the client role is ADMINISTRATOR
 */
@ParentSecured
@Provider
@Priority(SecurityContextPriority.PARENT)
public class ParentSecurityContext implements ContainerRequestFilter {

  /**
   * Filter requests that do not match the following security conditions: a) the client is
   * authenticated. b) the client role is PARENT.
   *
   * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
   * HTTP header.
   *
   * <p>If any of the above conditions are not met the request is dropped and the client is returned
   * a UNAUTHORIZED Error Response.
   */
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    User currentUser = AuthenticationSession.getCurrentUser(requestContext);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
      return;
    }

    System.out.println("Security context got ROLE: " + String.valueOf(currentUser.getRole()));
    // if the user logged has not the correct role
    if (!currentUser.getRole().equals(User.Role.PARENT)) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
      return;
    }
  }
}

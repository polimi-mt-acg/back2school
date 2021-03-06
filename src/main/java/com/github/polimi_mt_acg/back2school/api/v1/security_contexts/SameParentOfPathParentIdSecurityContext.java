package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecurityContext.getPathParameter;

/**
 * SameParentOfPathParentIdSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "SameParentOfPathParentId-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) if client role is PARENT, then it must correspond to the accessed path parent id
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@SameParentOfPathParentIdSecured
@Provider
@Priority(SecurityContextPriority.SAME_PARENT_OF_PATH_PARENT_ID)
public class SameParentOfPathParentIdSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    User currentUser = AuthenticationSession.getCurrentUser(requestContext);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new StatusResponse(Response.Status.UNAUTHORIZED, "Invalid session"))
              .build());
      return;
    }

    // if the user logged in has not the correct role
    if (!currentUser.getRole().equals(Role.PARENT)) {
      return; // Move control to next filter
    }
    // here currentUser has role PARENT

    // get the parent id
    Integer parentId = getPathParameter("parentId", requestContext);

    if (currentUser.getId() != parentId) {
      requestContext.abortWith(
          Response.status(Response.Status.FORBIDDEN)
              .entity(new StatusResponse(Response.Status.FORBIDDEN, "User not allowed"))
              .build());
    }
  }
}

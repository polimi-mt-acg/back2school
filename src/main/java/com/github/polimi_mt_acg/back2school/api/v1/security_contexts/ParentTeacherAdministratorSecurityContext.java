package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * ParentTeacherAdministratorSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "ParentsOrTeachersOrAdministrators-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) the client role is PARENT or TEACHER or ADMINISTRATOR
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@ParentTeacherAdministratorSecured
@Provider
@Priority(SecurityContextPriority.PARENT_TEACHER_ADMINISTRATOR)
public class ParentTeacherAdministratorSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    User currentUser = AuthenticationSession.getCurrentUser(requestContext);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new StatusResponse(Response.Status.UNAUTHORIZED, "Not a valid session"))
              .build());
      return;
    }

    // if the user logged in has not the correct role
    if (!currentUser.getRole().equals(Role.PARENT)
        && !currentUser.getRole().equals(Role.TEACHER)
        && !currentUser.getRole().equals(Role.ADMINISTRATOR)) {
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new StatusResponse(Response.Status.UNAUTHORIZED, "User not allowed"))
              .build());
      return;
    }
  }
}

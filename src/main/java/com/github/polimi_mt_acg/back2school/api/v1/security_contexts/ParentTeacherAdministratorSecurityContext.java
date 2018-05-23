package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * AdministratorSecurityContext implements a request filter for JAX-RS REST APIs. It implements a
 * "Administrators-only" security policy. A REST API that is annotated with @AdministratorSecured
 * can only be accessed if: a) the client is authenticated. b) the client role is ADMINISTRATOR
 */
@ParentTeacherAdministratorSecured
@Provider
@Priority(SecurityContextPriority.PARENT_TEACHER_ADMINISTRATOR)
public class ParentTeacherAdministratorSecurityContext implements ContainerRequestFilter {

  /**
   * Filter requests that do not match the following security conditions: a) the client is
   * authenticated. b) the client role is PARENT/TEACHER/ADMINISTRATOR.
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

    // if the user logged has not the correct role
    User.Role[] allowed = {Role.PARENT, Role.TEACHER, Role.ADMINISTRATOR};
    boolean is_allowed = false;
    for (Role role : allowed) {
      if (currentUser.getRole() == role) {
        is_allowed = true;
        break;
      }
    }

    if (!is_allowed) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }
}
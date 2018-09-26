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
 * SameTeacherOfPathTeacherIdSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "SameTeacherOfPathTeacherId-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) if client role is TEACHER, then it must correspond to the accessed path teacher id
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@SameTeacherOfPathTeacherIdSecured
@Provider
@Priority(SecurityContextPriority.SAME_PARENT_OF_PATH_PARENT_ID)
public class SameTeacherOfPathTeacherIdSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
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
    if (!currentUser.getRole().equals(Role.TEACHER)) {
      return; // Move control to next filter
    }
    // here currentUser has role TEACHER

    // get the teacher id
    Integer teacherId = getPathParameter("teacherId", requestContext);

    if (currentUser.getId() != teacherId) {
      requestContext.abortWith(
          Response.status(Response.Status.FORBIDDEN)
              .entity(new StatusResponse(Response.Status.FORBIDDEN, "User not allowed"))
              .build());
    }
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import static com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecurityContext.getPathParameter;

/**
 * ParentOfStudentSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "ParentOfAccessedStudent-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) if client role is PARENT then the user is required to be a parent of the accessed student
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@ParentOfStudentSecured
@Provider
@Priority(SecurityContextPriority.PARENT_OF_STUDENT)
public class ParentOfStudentSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws InvalidTemplateParameterException {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    User currentUser = AuthenticationSession.getCurrentUser(requestContext, session);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      session.getTransaction().commit();
      session.close();
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new StatusResponse(Response.Status.UNAUTHORIZED, "Not a valid session"))
              .build());
      return;
    }

    // if the user logged in is not on the checked role
    if (!currentUser.getRole().equals(Role.PARENT)) {
      session.getTransaction().commit();
      session.close();
      return; // Move control to next filter
    }
    // here currentUser has role PARENT

    // get studentId parameter from path
    Integer studentId = getPathParameter("studentId", requestContext);

    for (User child : currentUser.getChildren()) {
      if (child.getId() == studentId) {
        // found a children id corresponding to path studentId
        session.getTransaction().commit();
        session.close();
        return; // Move control to next filter
      }
    }

    // if arrived here, not a children of parent
    session.getTransaction().commit();
    session.close();
    requestContext.abortWith(
        Response.status(Response.Status.UNAUTHORIZED)
            .entity(new StatusResponse(Status.UNAUTHORIZED, "Access to the resource not allowed"))
            .build());
  }
}

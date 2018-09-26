package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.InvalidTemplateParameterException;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.SameTeacherSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.SecurityContextPriority;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.io.IOException;
import java.util.Optional;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

@SameTeacherSecured
@Provider
@Priority(SecurityContextPriority.SAME_TEACHER)
public class SameTeacherSecurityContext implements ContainerRequestFilter {

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
    if (currentUser.getRole() != Role.TEACHER) {
      return; // Move control to next filter
    }

    // Get the teacher id template parameter
    UriInfo uriInfo = requestContext.getUriInfo();
    MultivaluedMap<String, String> pathParameter = uriInfo.getPathParameters();

    if (!pathParameter.containsKey("id")) {
      throw new InvalidTemplateParameterException(
          "Could not find 'id' template parameter in URI. Maybe you annotated a non /teachers/{id}/* REST endpoint?");
    }

    // Get teacher id from request's URI
    int teacherId = Integer.parseInt(pathParameter.getFirst("id"));
    Optional<User> teacherOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, teacherId);
    if (!teacherOpt.isPresent()) {
      requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
    }

    if (currentUser.getId() != teacherOpt.get().getId()) {
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
    }
  }
}

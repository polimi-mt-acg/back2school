package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.InvalidTemplateParameterException;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.SecurityContextPriority;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.io.IOException;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.hibernate.Session;

@SameParentSecured
@Provider
@Priority(SecurityContextPriority.SAME_PARENT)
public class SameParentSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    User currentUser = AuthenticationSession.getCurrentUser(requestContext, session);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
      return;
    }

    // if the user logged has not the correct role
    if (currentUser.getRole() != Role.PARENT) {
      return; // Move control to next filter
    }

    // Get the parent id template parameter
    UriInfo uriInfo = requestContext.getUriInfo();
    MultivaluedMap<String, String> pathParameter = uriInfo.getPathParameters();

    if (!pathParameter.containsKey("id")) {
      throw new InvalidTemplateParameterException(
              "Could not find 'id' template parameter in URI. Maybe you annotated a non /parents/{id}/* REST endpoint?");
    }

    // Get parent id from request's URI
    int parentId = Integer.parseInt(pathParameter.getFirst("id"));
    List<User> result =
            DatabaseHandler.getInstance()
                    .getListSelectFromWhereEqual(User.class, User_.id, parentId, session);
    if (result.isEmpty()) {
      requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
    }

    // Check is requested parent is the same requesting parent
    boolean isSameParent = false;
    if (currentUser.getId() == result.get(0).getId())
    {
      isSameParent = true;
    }

    session.getTransaction().commit();
    session.close();

    if (!isSameParent) {
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
    }
  }
}

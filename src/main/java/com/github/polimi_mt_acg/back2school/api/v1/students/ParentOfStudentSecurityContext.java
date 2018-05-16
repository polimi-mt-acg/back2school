package com.github.polimi_mt_acg.back2school.api.v1.students;

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

@ParentOfStudentSecured
@Provider
@Priority(SecurityContextPriority.PARENT_OF_STUDENT)
public class ParentOfStudentSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    User parent = AuthenticationSession.getCurrentUser(requestContext, session);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (parent == null) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
      return;
    }

    // if the user logged has not the correct role
    if (parent.getRole() != Role.PARENT) {
      return; // Move control to next filter
    }

    // Get the student id template parameter
    UriInfo uriInfo = requestContext.getUriInfo();
    MultivaluedMap<String, String> pathParameter = uriInfo.getPathParameters();

    if (!pathParameter.containsKey("id")) {
      throw new InvalidTemplateParameterException(
          "Could not find 'id' template parameter in URI. Maybe you annotated a non /students/{id}/* REST endpoint?");
    }

    // Get student id from request's URI
    int studentId = Integer.parseInt(pathParameter.getFirst("id"));
    List<User> result =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.id, studentId, session);
    if (result.isEmpty()) {
      requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
    }

    // Check is requested student is child of requesting parent
    User student = result.get(0);
    List<User> children = parent.getChildren();
    boolean isParent = false;
    for (User child : children) {
      if (child.getId() == student.getId()) {
        isParent = true;
        break;
      }
    }

    session.getTransaction().commit();
    session.close();

    if (!isParent) {
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
    }
  }
}

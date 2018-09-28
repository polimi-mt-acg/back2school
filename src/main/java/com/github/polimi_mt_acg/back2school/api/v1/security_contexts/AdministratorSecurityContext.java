package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * AdministratorSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "Administrators-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) the client role is ADMINISTRATOR
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@AdministratorSecured
@Provider
@Priority(SecurityContextPriority.ADMINISTRATOR)
public class AdministratorSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
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
    if (!currentUser.getRole().equals(User.Role.ADMINISTRATOR)) {
      requestContext.abortWith(
          Response.status(Response.Status.FORBIDDEN)
              .entity(new StatusResponse(Response.Status.FORBIDDEN, "User not allowed"))
              .build());
      return;
    }
  }

  /**
   * Retrieve a path parameter value by its key.
   * @param parameterKey The name of the path parameter of which get the value.
   * @param crc The ContainerRequestContext of the filtered request.
   * @return The value of the parameter.
   * @throws InvalidTemplateParameterException when parameterKey value not found to be a parameter of path of the annotated endpoint.
   */
  static Integer getPathParameter(String parameterKey, ContainerRequestContext crc)
      throws InvalidTemplateParameterException {
    // Get the Map of path parameters
    MultivaluedMap<String, String> pathParameter = crc.getUriInfo().getPathParameters();

    if (!pathParameter.containsKey(parameterKey)) {
      String msg =
          String.format(
              "ERROR! Could not find parameter \"%s\" in annotated endpoint: %s.\n"
                  + "Does the URI you have annotated contain the \"%1$s\" parameter?\n",
              parameterKey, crc.getUriInfo().getPath());
      System.out.println(msg);
      throw new InvalidTemplateParameterException(msg);
    }

    // Get parameter from request's URI
    return Integer.parseInt(pathParameter.getFirst(parameterKey));
  }
}

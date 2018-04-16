package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

@AdministratorSecured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AdministratorSecurityContext implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Get the Authorization header from the request
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        // Validate the Authorization header
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            abortWithUnauthorized(requestContext);
        } else {
            // TODO: Should we have a more complex auth header?
            String token = authorizationHeader.trim();

            if (!isTokenValid(token)) {
                abortWithUnauthorized(requestContext);
            }
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        // Abort the filter chain with a 401 status code response
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED).build());
    }

    private boolean isTokenValid(String token) {
        // Check if the token was issued by the server and if it's not expired.
        // If so, check if the User is an admin. If not, abort.
        Session session = DatabaseHandler.getInstance().getNewSession();
        session.beginTransaction();
        List<AuthenticationSession> results = DatabaseHandler.getInstance()
                .getListSelectFromWhereEqual(
                        AuthenticationSession.class,
                        AuthenticationSession_.token,
                        token,
                        session);

        boolean isValid = false;
        if (results.size() > 0) {
            AuthenticationSession authSession = results.get(0);
            // TODO: Check token expiration
            if (!authSession.isCancelled()) {
                User.Role role = authSession.getUser().getRole();
                if (role == User.Role.ADMINISTRATOR) {
                    isValid = true;
                }
            }
        }
        session.getTransaction().commit();
        session.close();
        return isValid;
    }
}

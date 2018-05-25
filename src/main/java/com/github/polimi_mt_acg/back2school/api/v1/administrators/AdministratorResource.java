package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * JAX-RS Resource for administrators entity (a {@link User User} with (A {@link User.Role Role}
 * equal to {@code ADMINISTRATOR}).
 */
@Path("administrators")
public class AdministratorResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getAdministrators() {
    List<User> admins =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.ADMINISTRATOR);

    AdministratorResponse response = new AdministratorResponse(admins);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }
}

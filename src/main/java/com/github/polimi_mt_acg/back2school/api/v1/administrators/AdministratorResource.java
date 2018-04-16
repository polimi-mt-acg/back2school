package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("administrators")
public class AdministratorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @AdministratorSecured
    public AdministratorResponse getAdministrators() {
        User admin1 = new User();
        admin1.setRole(User.Role.ADMINISTRATOR);
        admin1.setName("Admin 1 Name");
        admin1.setSurname("Admin 1 Surname");
        admin1.setEmail("admin1@email.com");

        User admin2 = new User();
        admin2.setRole(User.Role.ADMINISTRATOR);
        admin2.setName("Admin 2 Name");
        admin2.setSurname("Admin 2 Surname");
        admin2.setEmail("admin2@email.com");

        List<User> admins = new ArrayList<>();
        admins.add(admin1);
        admins.add(admin2);

        return new AdministratorResponse(admins);
    }
}

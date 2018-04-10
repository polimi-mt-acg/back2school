package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/administrators")
public class AdministratorResource {

    @GET
    @Produces("text/html")
    public String getHtml() {
        return "mytest";
    }
}

package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/classrooms")
public class ClassroomResource {

    @GET
    @Produces("text/html")
    public String getHtml() {

        return "mytest";
    }
}

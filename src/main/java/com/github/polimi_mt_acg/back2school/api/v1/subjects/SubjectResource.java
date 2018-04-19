package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.json.JSONObject;

@Path("/subjects")
public class SubjectResource {

  String sbj;

  @GET
  @Produces("text/http")
  public String getHtml() {
    List<Subject> sbjList = DatabaseHandler.getInstance().getListSelectFrom(Subject.class);
    String name;
    String description;
    String id;
    JSONObject obj1 = new JSONObject();
    JSONObject obj2 = new JSONObject();
    for (Subject sbj : sbjList) {
      name = sbj.getName();
      description = sbj.getDescription();
      id = Integer.toString(sbj.getId());
      obj2.put("name", name);
      obj2.put("description", description);
      obj1.put(id, obj2);
    }
    return String.valueOf(obj1);
  }

  @POST
  @Consumes("text/html")
  public void postClichedSubject(String name, String description) {
    // store new subject
  }

  @GET
  @Path("/{id}")
  public Integer getSubject(@PathParam("id") int id) {
    return id;
  }

  @PUT
  @Path("/{id}")
  public void putDescription(@PathParam("id") int id) {
    //
  }

  @PUT
  @Path("/{id}")
  public void putName(@PathParam("id") int id) {
    //
  }
}

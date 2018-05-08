package com.github.polimi_mt_acg.back2school.api.v1.subjects;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.polimi_mt_acg.back2school.api.v1.administrators.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("subjects")
public class SubjectResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public SubjectResponse getSubjects()throws JsonProcessingException {
    List<Subject> subjects =
        DatabaseHandler.getInstance()
            .getListSelectFrom(Subject.class);

    return new SubjectResponse(subjects);
  }
}

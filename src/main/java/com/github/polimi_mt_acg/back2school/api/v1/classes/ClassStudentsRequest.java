package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.ws.rs.core.Response;
import java.util.Optional;

public class ClassStudentsRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;

  @JsonProperty("student_id")
  private Integer studentId;

  public Integer getStudentId() {
    return studentId;
  }

  public void setStudentId(Integer studentId) {
    this.studentId = studentId;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getStudentId() == null || getStudentId() == 0) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: student_id"))
              .build();
      return false;
    }

    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, getStudentId());
    if (!userOpt.isPresent() || !userOpt.get().getRole().equals(User.Role.STUDENT)) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Response.Status.BAD_REQUEST, "Unknown student id"))
              .build();
      return false;
    }

    return true;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPostResponse() {
    return invalidPostResponse;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPut(Integer id) {
    return false;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPutResponse() {
    return null;
  }
}

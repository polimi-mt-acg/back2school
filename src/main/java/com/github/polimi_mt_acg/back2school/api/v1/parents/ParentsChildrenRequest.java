package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.ws.rs.core.Response;
import java.util.Optional;

public class ParentsChildrenRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;

  @JsonProperty("child_id")
  private Integer childId;

  public Integer getChildId() {
    return childId;
  }

  public void setChildId(Integer childId) {
    this.childId = childId;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getChildId() == null || getChildId() == 0) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: child_id"))
              .build();
      return false;
    }

    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, getChildId());
    if (!userOpt.isPresent() || !userOpt.get().getRole().equals(User.Role.STUDENT)) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Response.Status.BAD_REQUEST, "Unknown child id"))
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

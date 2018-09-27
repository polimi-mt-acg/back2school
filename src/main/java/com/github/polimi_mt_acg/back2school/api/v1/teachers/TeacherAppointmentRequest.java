package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.Appointment;
import com.github.polimi_mt_acg.back2school.model.Appointment_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Optional;

public class TeacherAppointmentRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;
  @JsonIgnore private Response invalidPutResponse;

  @JsonProperty("parent_id")
  private Integer parentId;

  @JsonProperty("datetime_start")
  private LocalDateTime datetimeStart;

  @JsonProperty("datetime_end")
  private LocalDateTime datetimeEnd;

  private Appointment.Status status = Appointment.Status.REQUESTED;

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public LocalDateTime getDatetimeStart() {
    return datetimeStart;
  }

  public void setDatetimeStart(LocalDateTime datetimeStart) {
    this.datetimeStart = datetimeStart;
  }

  public LocalDateTime getDatetimeEnd() {
    return datetimeEnd;
  }

  public void setDatetimeEnd(LocalDateTime datetimeEnd) {
    this.datetimeEnd = datetimeEnd;
  }

  public Appointment.Status getStatus() {
    return status;
  }

  public void setStatus(Appointment.Status status) {
    this.status = status;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getParentId() == null || getParentId() == 0) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: parent_id"))
              .build();
      return false;
    }

    Optional<User> userOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, getParentId());
    if (!userOpt.isPresent() || !userOpt.get().getRole().equals(User.Role.PARENT)) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Response.Status.BAD_REQUEST, "Unknown parent id"))
              .build();
      return false;
    }

    if (getDatetimeStart() == null || getDatetimeStart().toString().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: datetime_start"))
              .build();
      return false;
    }

    if (getDatetimeEnd() == null || getDatetimeEnd().toString().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: datetime_end"))
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
    Optional<Appointment> appointmentOpt =
        DatabaseHandler.fetchEntityBy(Appointment.class, Appointment_.id, id);
    if (!appointmentOpt.isPresent()) {
      invalidPutResponse =
          Response.status(Response.Status.NOT_FOUND)
              .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown appointment id"))
              .build();
      return false;
    }

    if (!isValidForPost()) {
      invalidPutResponse = invalidPostResponse;
      return false;
    }

    if (getStatus() == null || getStatus().toString().isEmpty()) {
      invalidPutResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: status"))
              .build();
      return false;
    }

    return true;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPutResponse() {
    return invalidPutResponse;
  }
}

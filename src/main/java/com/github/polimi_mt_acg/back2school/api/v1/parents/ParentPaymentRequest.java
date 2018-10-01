package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.Payment;

import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

public class ParentPaymentRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;

  private Payment.Type type;

  @JsonProperty("datetime_requested")
  private LocalDateTime datetimeRequested;

  @JsonProperty("datetime_done")
  private LocalDateTime datetimeDone;

  @JsonProperty("datetime_deadline")
  private LocalDateTime datetimeDeadline;

  private Boolean done;
  private String subject;
  private String description;
  private double amount;

  public Payment.Type getType() {
    return type;
  }

  public void setType(Payment.Type type) {
    this.type = type;
  }

  public LocalDateTime getDatetimeRequested() {
    return datetimeRequested;
  }

  public void setDatetimeRequested(LocalDateTime datetimeRequested) {
    this.datetimeRequested = datetimeRequested;
  }

  public LocalDateTime getDatetimeDone() {
    return datetimeDone;
  }

  public void setDatetimeDone(LocalDateTime datetimeDone) {
    this.datetimeDone = datetimeDone;
  }

  public LocalDateTime getDatetimeDeadline() {
    return datetimeDeadline;
  }

  public void setDatetimeDeadline(LocalDateTime datetimeDeadline) {
    this.datetimeDeadline = datetimeDeadline;
  }

  public Boolean getDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getType() == null || getType().toString().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: type"))
              .build();
      return false;
    }

    if (getDatetimeDeadline() == null || getDatetimeDeadline().toString().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: datetime_deadline"))
              .build();
      return false;
    }

    if (getSubject() == null || getSubject().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: subject"))
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

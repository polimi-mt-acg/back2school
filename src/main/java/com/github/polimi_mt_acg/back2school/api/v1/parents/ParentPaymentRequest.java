package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.model.Payment;

import java.time.LocalDateTime;

public class ParentPaymentRequest {

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
}

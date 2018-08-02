package com.github.polimi_mt_acg.back2school.api.v1.parents;

import java.time.LocalDateTime;

public class PostParentPaymentRequest {
  private String placedByEmail;
  private String assignedToEmail;
  private double amount ;
  private String description;
  private String subject;
  private LocalDateTime datetimeRequested;
  private LocalDateTime datetimeDeadline;



  public String getPlacedByEmail() {
    return placedByEmail;
  }

  public void setPlacedByEmail(String placedByEmail) {
    this.placedByEmail = placedByEmail;
  }

  public String getAssignedToEmail() {
    return assignedToEmail;
  }

  public void setAssignedToEmail(String assignedToEmail) {
    this.assignedToEmail = assignedToEmail;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public LocalDateTime getDatetimeRequested() {
    return datetimeRequested;
  }

  public void setDatetimeRequested(LocalDateTime datetimeRequested) {
    this.datetimeRequested = datetimeRequested;
  }

  public LocalDateTime getDatetimeDeadline() {
    return datetimeDeadline;
  }

  public void setDatetimeDeadline(LocalDateTime datetimeDeadline) {
    this.datetimeDeadline = datetimeDeadline;
  }

}

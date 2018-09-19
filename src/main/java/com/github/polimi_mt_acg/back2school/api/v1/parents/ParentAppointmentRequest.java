package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.model.Appointment;

import java.time.LocalDateTime;


public class ParentAppointmentRequest {

  @JsonProperty("teacher_id")
  private Integer teacherId;

  @JsonProperty("datetime_start")
  private LocalDateTime datetimeStart;

  @JsonProperty("datetime_end")
  private LocalDateTime datetimeEnd;
  private Appointment.Status status;

  public Integer getTeacherId() {
    return teacherId;
  }

  public void setTeacherId(Integer teacherId) {
    this.teacherId = teacherId;
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
}

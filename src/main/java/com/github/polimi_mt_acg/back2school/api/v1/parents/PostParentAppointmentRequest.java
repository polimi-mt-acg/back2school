package com.github.polimi_mt_acg.back2school.api.v1.parents;

import java.time.LocalDateTime;


public class PostParentAppointmentRequest {
  private String teacherEmail;
  private LocalDateTime datetimeStart;
  private LocalDateTime datetimeEnd;

  public String getTeacherEmail() {
    return teacherEmail;
  }

  public void setTeacherEmail(String teacherEmail) {
    this.teacherEmail = teacherEmail;
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
}

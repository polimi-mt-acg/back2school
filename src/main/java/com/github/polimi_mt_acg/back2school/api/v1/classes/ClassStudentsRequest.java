package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassStudentsRequest {

  @JsonProperty("student_id")
  private Integer studentId;

  public Integer getStudentId() {
    return studentId;
  }

  public void setStudentId(Integer studentId) {
    this.studentId = studentId;
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.github.polimi_mt_acg.back2school.model.User;

public class PostStudentRequest {
  private User student;
  private String parentEmail;

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
  }

  public String getParentEmail() {
    return parentEmail;
  }

  public void setParentEmail(String parentEmail) {
    this.parentEmail = parentEmail;
  }
}

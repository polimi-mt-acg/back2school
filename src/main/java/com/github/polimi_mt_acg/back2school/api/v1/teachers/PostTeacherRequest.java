package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.model.User;

public class PostTeacherRequest {
  private User teacher;

  // user password is serialized separately from the entity because it is not
  // serialized and serialized in the entity itself
  private String userPassword;

  public User getTeacher() {
    this.teacher.setSeedPassword(this.userPassword);
    return teacher;
  }

  public void setTeacher(User teacher) {
    this.teacher = teacher;
  }

  public void setTeacherAndPassword(User teacher, String seedPassword) {
    this.userPassword = seedPassword;
    setTeacher(teacher);
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }
}

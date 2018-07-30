package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.model.User;

public class PostParentRequest {
  // user password is serialized separately from the entity because it is not
  // serialized and serialized in the entity itself
  private String userPassword;

  private User parent;
  private String studentEmail;

  public User getParent() {
    this.parent.setSeedPassword(this.userPassword);
    return parent;
  }

  public void setParent(User parent) {
    this.parent = parent;
  }

  public String getStudentEmail() {
    return studentEmail;
  }

  public void setStudentEmail(String studentEmail) {
    this.studentEmail = studentEmail;
  }

  public void setParentAndPassword(User parent, String seedPassword) {
    this.userPassword = seedPassword;
    setParent(parent);
  }

  public String getUserPassword() {
    return userPassword;
  }

  public void setUserPassword(String userPassword) {
    this.userPassword = userPassword;
  }

}

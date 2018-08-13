package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.model.User;

import java.util.List;

public class PostChildrenRequest {
  private User parent;
  private User student;

  public User getParent() {
    return parent;
  }

  public void setParent(User parent) {
    this.parent= parent;
  }

  public User getStudent() {
    return student;
  }

  public void setStudent(User student) {
    this.student = student;
  }

}

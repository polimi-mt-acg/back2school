package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.model.User;

public class PostUserRequest {

  private User user;
  // user password is serialized separately from the entity because it is not
  // serialized and deserialized automatically within the entity
  private String password;

  public User getUser() {
    this.user.setSeedPassword(this.password);
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}

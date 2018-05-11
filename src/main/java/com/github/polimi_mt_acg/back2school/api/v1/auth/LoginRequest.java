package com.github.polimi_mt_acg.back2school.api.v1.auth;

/** The login credentials. A client can authenticate itself providing its EMAIL and PASSWORD. */
public class LoginRequest {

  public String email;
  public String password;

  public LoginRequest() {}

  public LoginRequest(String email, String password) {
    this.email = email;
    this.password = password;
  }
}

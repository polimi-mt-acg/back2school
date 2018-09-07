package com.github.polimi_mt_acg.back2school.api.v1.auth;

public class LoginResponse {

  public String status;
  public String token;

  public LoginResponse(String status, String token) {
    this.status = status;
    this.token = token;
  }
}

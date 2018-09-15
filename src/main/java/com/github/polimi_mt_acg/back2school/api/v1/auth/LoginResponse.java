package com.github.polimi_mt_acg.back2school.api.v1.auth;

public class LoginResponse {

  public String status;
  public String token;

  public LoginResponse() {}

  public LoginResponse(String status, String token) {
    this.status = status;
    this.token = token;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}

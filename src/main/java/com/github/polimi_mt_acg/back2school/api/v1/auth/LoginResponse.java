package com.github.polimi_mt_acg.back2school.api.v1.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {

  public String status;
  public String token;

  @JsonProperty("authorization_header")
  public String authorizationHeader;

  public LoginResponse() {}

  public LoginResponse(String status, String token) {
    this.status = status;
    this.token = token;
    this.authorizationHeader = "Bearer " + token;
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

  public String getAuthorizationHeader() {
    return "Bearer " + token;
  }

  public void setAuthorizationHeader(String authorizationHeader) {
    this.authorizationHeader = authorizationHeader;
  }
}

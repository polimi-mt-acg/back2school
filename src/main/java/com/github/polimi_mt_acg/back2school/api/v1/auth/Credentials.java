package com.github.polimi_mt_acg.back2school.api.v1.auth;

/** The login credentials. A client can authenticate itself providing its EMAIL and PASSWORD. */
public class Credentials {

  public String email;
  public String password;

  public Credentials() {}

  public Credentials(String email, String password) {
    this.email = email;
    this.password = password;
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.auth;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /auth/logout.
 * The serialized JSON format has the following structure:
 * <code>{
 *   "status" : "ok"
 * }</code>
 */
@XmlRootElement
public class LogoutResponse {

  @XmlElement
  private String status = "logged out";
  private String reason = "user request";

  public LogoutResponse() {}

  public LogoutResponse(String reason) {
    this.reason = reason;
  }
}

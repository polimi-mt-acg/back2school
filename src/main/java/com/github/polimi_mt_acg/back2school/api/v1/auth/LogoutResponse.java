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

  private String status = "LOGGED_OUT";
  private String reason = "USER_REQUEST";

  public LogoutResponse() {}

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}

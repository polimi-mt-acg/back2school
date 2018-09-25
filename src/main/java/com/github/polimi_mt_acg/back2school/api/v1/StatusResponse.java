package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResponse {

  @JsonIgnore private Response.Status responseStatus;

  @JsonProperty("status_code")
  private Integer statusCode;

  @JsonProperty("status_reason")
  private String statusReason;

  private String description;

  public StatusResponse(Response.Status responseStatus) {
    setResponseStatus(responseStatus);
  }

  public StatusResponse(Response.Status responseStatus, String description) {
    setResponseStatus(responseStatus);
    this.description = description;
  }

  public Response.Status getResponseStatus() {
    return responseStatus;
  }

  public void setResponseStatus(Response.Status responseStatus) {
    this.responseStatus = responseStatus;
    setStatusCode(responseStatus.getStatusCode());
    setStatusReason(responseStatus.getReasonPhrase());
  }

  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public String getStatusReason() {
    return statusReason;
  }

  public void setStatusReason(String statusReason) {
    this.statusReason = statusReason;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}

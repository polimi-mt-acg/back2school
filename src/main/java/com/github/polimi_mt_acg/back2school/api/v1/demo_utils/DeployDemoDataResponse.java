package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

public class DeployDemoDataResponse {

  private String status;
  private String duration;

  DeployDemoDataResponse(String status, String duration) {
    this.status = status;
    this.duration = duration;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getDuration() {
    return duration;
  }

  public void setDuration(String duration) {
    this.duration = duration;
  }
}

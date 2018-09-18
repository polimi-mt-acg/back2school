package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class DemoUtilsActionResponse {

  @JsonProperty("performed_actions")
  private List<String> actions = new ArrayList<>();
  private String status;
  private String duration;
  private String info = "All previously active sessions has been terminated. Please, login again.";

  public List<String> getActions() {
    return actions;
  }

  public void addAction(String action) {
    this.actions.add(action);
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

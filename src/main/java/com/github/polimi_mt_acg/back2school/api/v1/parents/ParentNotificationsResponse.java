package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class ParentNotificationsResponse {
  private  List<URI> notifications;

  @JsonProperty
  public List<URI> getNotifications() {
    return notifications;
  }

  @JsonProperty public void setNotifications(List<URI> notifications) {
    this.notifications = notifications;
  }
}

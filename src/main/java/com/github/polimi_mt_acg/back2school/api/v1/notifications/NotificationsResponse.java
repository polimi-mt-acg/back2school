package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
public class NotificationsResponse {

  @JsonProperty
  private List<URI> notifications;

  public List<URI> getNotifications() {
    return notifications;
  }

  public void setNotifications(List<URI> notifications) {
    this.notifications = notifications;
  }
}

package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class NotificationsReadJSONTemplate implements JSONTemplateInterface {

  @JsonProperty("notifications_read")
  public List<SeedEntityNotificationRead> notificationsRead;

  @Override
  public List<?> getEntities() {
    return notificationsRead;
  }

  public List<SeedEntityNotificationRead> getNotificationsRead() {
    return notificationsRead;
  }

  public void setNotificationsRead(List<SeedEntityNotificationRead> notificationsRead) {
    this.notificationsRead = notificationsRead;
  }
}

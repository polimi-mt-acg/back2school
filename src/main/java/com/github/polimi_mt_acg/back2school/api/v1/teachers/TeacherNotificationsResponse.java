package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.model.Notification;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TeacherNotificationsResponse {
  private List<Entity> notifications = new ArrayList<>();

  public List<Entity> getNotifications() {
    return notifications;
  }

  public void setNotifications(List<Entity> notifications) {
    this.notifications = notifications;
  }

  public static class Entity {

    private String subject;

    private Notification.Status status = Notification.Status.UNREAD;

    private URI url;

    public String getSubject() {
      return subject;
    }

    public void setSubject(String subject) {
      this.subject = subject;
    }

    public Notification.Status getStatus() {
      return status;
    }

    public void setStatus(Notification.Status status) {
      this.status = status;
    }

    public URI getUrl() {
      return url;
    }

    public void setUrl(URI url) {
      this.url = url;
    }
  }
}

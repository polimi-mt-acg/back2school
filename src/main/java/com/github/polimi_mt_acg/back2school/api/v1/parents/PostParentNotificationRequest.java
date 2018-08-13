package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.model.Class;

import java.time.LocalDateTime;


public class PostParentNotificationRequest {
  private String creatorEmail;
  private LocalDateTime datetime;
  private String subject;
  private String text;



  public String getCreatorEmail() {
    return creatorEmail;
  }

  public void setCreatorEmail(String creatorEmail) {
    this.creatorEmail = creatorEmail;
  }

  public LocalDateTime getDatetime() {
    return datetime;
  }

  public void setDatetime(LocalDateTime datetime) {
    this.datetime = datetime;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}

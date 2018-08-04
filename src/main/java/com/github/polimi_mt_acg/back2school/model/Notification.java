package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = javax.persistence.DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @Type(value = NotificationClassParent.class, name = "class_parent"),
  @Type(value = NotificationClassTeacher.class, name = "class_teacher"),
  @Type(value = NotificationGeneralParents.class, name = "general_parents"),
  @Type(value = NotificationGeneralTeachers.class, name = "general_teachers"),
  @Type(value = NotificationPersonalParent.class, name = "personal_parent"),
  @Type(value = NotificationPersonalTeacher.class, name = "personal_teacher")
})
public class Notification implements DeserializeToPersistInterface {

  @Transient private String seedCreatorEmail;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @ManyToOne
  @JoinColumn(name = "creator_id", foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
  private User creator;

  @Column(name = "datetime")
  private LocalDateTime datetime = LocalDateTime.now();

  @Column(name = "subject")
  private String subject;

  @Column(name = "text")
  private String text;

  @JsonIgnore
  public int getId() {
    return id;
  }

  @JsonIgnore
  public void setId(int id) {
    this.id = id;
  }

  @JsonProperty
  public User getCreator() {
    return creator;
  }

  @JsonProperty
  public void setCreator(User creator) {
    this.creator = creator;
  }

  @JsonProperty
  public LocalDateTime getDatetime() {
    return datetime;
  }

  @JsonProperty
  public void setDatetime(LocalDateTime datetime) {
    this.datetime = datetime;
  }

  @JsonProperty
  public String getSubject() {
    return subject;
  }

  @JsonProperty
  public void setSubject(String subject) {
    this.subject = subject;
  }

  @JsonProperty
  public String getText() {
    return text;
  }

  @JsonProperty
  public void setText(String text) {
    this.text = text;
  }

  @JsonIgnore
  public String getSeedCreatorEmail() {
    return seedCreatorEmail;
  }

  @JsonProperty
  public void setSeedCreatorEmail(String seedCreatorEmail) {
    this.seedCreatorEmail = seedCreatorEmail;
  }

  @Override
  public void prepareToPersist() {
    seedAssociateCreator();
  }

  private void seedAssociateCreator() {
    if (seedCreatorEmail != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedCreatorEmail);
      if (users != null) {
        setCreator(users.get(0));
      }
    }
  }

  /**
   * Get the status (read or unread) with respect to a user.
   * @param user
   * @return
   */
  public Status getStatusWithRespectTo(User user) {
    Status status = Status.UNREAD;
    // if the current notification is found among those already read by the user,
    // then status is read
    for (Notification notification: user.getNotificationsRead()) {
      if (getId() == notification.getId()) {
        status = Status.READ;
        break;
      }
    }
    return status;
  }

  public enum Status {
    READ,
    UNREAD
  }

  /**
   * Notification class to send a new Notification. Since for any type of notification the strictly
   * required information from the client side are only the subject and text, the following is a
   * common request class.
   */
  public static class NotificationRequest {
    private String subject;

    private String text;

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
}

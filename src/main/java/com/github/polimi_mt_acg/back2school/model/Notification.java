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
  @Type(value = NotificationClassParent.class, name = "class_parent"),
  @Type(value = NotificationClassTeacher.class, name = "class_teacher"),
  @Type(value = NotificationGeneral.class, name = "general"),
  @Type(value = NotificationPersonalParent.class, name = "personal_parent"),
  @Type(value = NotificationPersonalTeacher.class, name = "personal_teacher")
})
public class Notification implements DeserializeToPersistInterface {

  @Transient @JsonIgnore public String seedCreatorEmail;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @ManyToOne
  @JoinColumn(name = "creator_id", foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
  private User creator;

  @Column(name = "datetime")
  private LocalDateTime datetime;

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
}

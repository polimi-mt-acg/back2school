package com.github.polimi_mt_acg.back2school.model;

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
public class Notification implements DeserializeToPersistInterface {

  @Transient
  public String seedCreatorEmail;
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

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
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

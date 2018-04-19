package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.RandomStringGenerator;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "authentication_session")
public class AuthenticationSession implements DeserializeToPersistInterface {

  @Transient public String seedUserEmail;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @ManyToOne
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "SESSION_USER_ID_FK"))
  private User user;

  @Column(name = "token")
  private String token = RandomStringGenerator.generateString();

  @Column(name = "datetime_last_interaction")
  private LocalDateTime datetimeLastInteraction = LocalDateTime.now();

  @Column(name = "cancelled")
  private boolean cancelled = false;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public LocalDateTime getDatetimeLastInteraction() {
    return datetimeLastInteraction;
  }

  public void setDatetimeLastInteraction(LocalDateTime datetimeLastInteraction) {
    this.datetimeLastInteraction = datetimeLastInteraction;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public void prepareToPersist() {
    seedAssociateUser();
  }

  private void seedAssociateUser() {
    if (seedUserEmail != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedUserEmail);
      if (users != null) {
        setUser(users.get(0));
      }
    }
  }
}

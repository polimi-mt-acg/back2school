package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "session")
public class AuthenticationSession {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "SESSION_USER_ID_FK"))
    private User user;

    @Column(name = "token")
    private String token = new RandomStringGenerator().generateString();

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

    public class RandomStringGenerator {
        public String generateString() {
            String uuid = UUID.randomUUID().toString();
            return uuid.replace("-", "");
        }
    }
}

package com.github.polimi_mt_acg.back2school.model;


import java.time.LocalDateTime;

public class Session {

    private int id;
    private int userId;
    private String token;
    private LocalDateTime datetimeLastInteraction;
    private boolean cancelled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

}

package com.github.polimi_mt_acg.back2school.model;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class Payment {

    private int id;
    private int placedBy;
    private int assignedBy;
    private int type;
    private LocalDateTime datetimeRequested;
    private LocalDateTime datetimeDone;
    private LocalDateTime datetimeDeadline;
    private boolean done;
    private String subject;
    private String description;
    private double amout;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(int placedBy) {
        this.placedBy = placedBy;
    }

    public int getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(int assignedBy) {
        this.assignedBy = assignedBy;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public LocalDateTime getDatetimeRequested() {
        return datetimeRequested;
    }

    public void setDatetimeRequested(LocalDateTime datetimeRequested) {
        this.datetimeRequested = datetimeRequested;
    }

    public LocalDateTime getDatetimeDone() {
        return datetimeDone;
    }

    public void setDatetimeDone(LocalDateTime datetimeDone) {
        this.datetimeDone = datetimeDone;
    }

    public LocalDateTime getDatetimeDeadline() {
        return datetimeDeadline;
    }

    public void setDatetimeDeadline(LocalDateTime datetimeDeadline) {
        this.datetimeDeadline = datetimeDeadline;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmout() {
        return amout;
    }

    public void setAmout(double amout) {
        this.amout = amout;
    }
}

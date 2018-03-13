package com.github.polimi_mt_acg.back2school.model;

import java.time.LocalDateTime;

public class Appointment {

    private int id;
    private int teacherId;
    private int parentId;
    private LocalDateTime datetimeStart;
    private LocalDateTime datetimeEnd;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public LocalDateTime getDatetimeStart() {
        return datetimeStart;
    }

    public void setDatetimeStart(LocalDateTime datetimeStart) {
        this.datetimeStart = datetimeStart;
    }

    public LocalDateTime getDatetimeEnd() {
        return datetimeEnd;
    }

    public void setDatetimeEnd(LocalDateTime datetimeEnd) {
        this.datetimeEnd = datetimeEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

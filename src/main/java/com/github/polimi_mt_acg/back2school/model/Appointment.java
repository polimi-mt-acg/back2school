package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TEACHER_ID_FK"))
    private int teacherId;

    @ManyToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "PARENT_ID_FK"))
    private int parentId;

    @Column(name = "datetime_start")
    private LocalDateTime datetimeStart;

    @Column(name = "datetime_end")
    private LocalDateTime datetimeEnd;

    @Column(name = "status")
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

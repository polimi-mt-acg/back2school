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
            foreignKey = @ForeignKey(name = "APPOINTMENT_TEACHER_ID_FK"))
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "APPOINTMENT_PARENT_ID_FK"))
    private User parent;

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

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public User getParent() {
        return parent;
    }

    public void setParent(User parent) {
        this.parent = parent;
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

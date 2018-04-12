package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "appointment")
public class Appointment implements DeserializeToPersistInterface {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "teacher_id",
            foreignKey = @ForeignKey(name = "APPOINTMENT_TEACHER_ID_FK"))
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "parent_id",
            foreignKey = @ForeignKey(name = "APPOINTMENT_PARENT_ID_FK"))
    private User parent;

    @Column(name = "datetime_start")
    private LocalDateTime datetimeStart;

    @Column(name = "datetime_end")
    private LocalDateTime datetimeEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.REQUESTED;

    @Transient
    public String seedTeacherEmail;

    @Transient
    public String seedParentEmail;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    enum Status {
        REQUESTED, COUNTERPROPOSED, AGREED
    }

    @Override
    public void prepareToPersist() {
        seedAssociateTeacher();
        seedAssociateParent();
    }

    private void seedAssociateTeacher() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTeacherEmail);
        if (users != null) {
            setTeacher(users.get(0));
        }
    }

    private void seedAssociateParent() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedParentEmail);
        if (users != null) {
            setParent(users.get(0));
        }
    }
}

package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lecture")
public class Lecture {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "subject_id",
            foreignKey = @ForeignKey(name = "LECTURE_SUBJECT_ID_FK"))
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id",
            foreignKey = @ForeignKey(name = "LECTURE_TEACHER_ID_FK"))
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "classroom_id",
            foreignKey = @ForeignKey(name = "LECTURE_CLASSROOM_ID_FK"))
    private Classroom classroom;

    // TODO: Add @ManyToOne association to Class
    private int classId;

    @Column(name = "datetime_start")
    private LocalDateTime datetimeStart;

    @Column(name = "datetime_end")
    private LocalDateTime datetimeEnd;

    public int getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public Classroom getClassroom() {
        return classroom;
    }

    public void setClassroom(Classroom classroom) {
        this.classroom = classroom;
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
}

package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "grade")
public class Grade {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "subject_id",
            foreignKey = @ForeignKey(name = "GRADE_SUBJECT_ID_FK"))
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id",
            foreignKey = @ForeignKey(name = "GRADE_TEACHER_ID_FK"))
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "student_id",
            foreignKey = @ForeignKey(name = "GRADE_STUDENT_ID_FK"))
    private User student;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "title")
    private String title;

    @Column(name = "grade")
    private double grade;

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

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }
}

package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

@Entity
@Table(name = "grade")
public class Grade implements DeserializeToPersistInterface {

    private final static Logger LOGGER =
            Logger.getLogger(Grade.class.getName());
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

    @Transient
    public String seedSubjectName;

    @Transient
    public String seedTeacherEmail;

    @Transient
    public String seedStudentEmail;

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

    @Override
    public void prepareToPersist() {
        seedAssociateSubject();
        seedAssociateTeacher();
        seedAssociateStudent();
    }

    private void seedAssociateSubject() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<Subject> subjects =
                dhi.getListSelectFromWhereEqual(Subject.class, Subject_.name, seedSubjectName);
        if (subjects != null) {
            setSubject(subjects.get(0));
        }
    }

    private void seedAssociateTeacher() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTeacherEmail);
        if (users != null) {
            setTeacher(users.get(0));
        }
    }

    private void seedAssociateStudent() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedStudentEmail);
        if (users != null) {
            setStudent(users.get(0));
        }
    }
}

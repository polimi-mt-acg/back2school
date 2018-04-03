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
    private String seedSubjectName;

    @Transient
    private String seedTeacherEmail;

    @Transient
    private String seedStudentEmail;

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
        associateSubject();
        associateTeacher();
        associateStudent();
    }

    private void associateSubject() {
        Session session = DatabaseHandler.getInstance().getNewSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<Subject> criteria = builder.createQuery(Subject.class);
        Root<Subject> root = criteria.from(Subject.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get(Subject_.name), this.seedSubjectName));

        List<Subject> entities = session.createQuery(criteria).getResultList();

        if (entities.size() != 0) {
            this.setSubject(entities.get(0));
        } else {
            LOGGER.info("NO subject found with name: " + String.valueOf(this.seedSubjectName));
        }
    }

    private void associateTeacher() {
        Session session = DatabaseHandler.getInstance().getNewSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get(User_.email), this.seedTeacherEmail));

        List<User> entities = session.createQuery(criteria).getResultList();

        if (entities.size() != 0) {
            this.setTeacher(entities.get(0));
        } else {
            LOGGER.info("NO teacher found with email: " + String.valueOf(this.seedTeacherEmail));
        }
    }

    private void associateStudent() {
        Session session = DatabaseHandler.getInstance().getNewSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get(User_.email), this.seedStudentEmail));

        List<User> entities = session.createQuery(criteria).getResultList();

        if (entities.size() != 0) {
            this.setStudent(entities.get(0));
        } else {
            LOGGER.info("NO student found with email: " + String.valueOf(this.seedStudentEmail));
        }
    }
}

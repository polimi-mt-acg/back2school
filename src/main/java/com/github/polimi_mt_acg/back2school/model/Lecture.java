package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "lecture")
public class Lecture implements DeserializeToPersistInterface {

  @Transient public String seedSubjectName;
  @Transient public String seedTeacherEmail;
  @Transient public String seedClassroomName;
  @Transient public String seedClassName;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @ManyToOne
  @JoinColumn(name = "subject_id", foreignKey = @ForeignKey(name = "LECTURE_SUBJECT_ID_FK"))
  private Subject subject;

  @ManyToOne
  @JoinColumn(name = "teacher_id", foreignKey = @ForeignKey(name = "LECTURE_TEACHER_ID_FK"))
  private User teacher;

  @ManyToOne
  @JoinColumn(name = "classroom_id", foreignKey = @ForeignKey(name = "LECTURE_CLASSROOM_ID_FK"))
  private Classroom classroom;

  @ManyToOne
  @JoinColumn(name = "class_id", foreignKey = @ForeignKey(name = "LECTURE_CLASS_ID_FK"))
  private Class class_;

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

  public Class getClass_() {
    return class_;
  }

  public void setClass_(Class class_) {
    this.class_ = class_;
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

  @Override
  public void prepareToPersist() {
    seedAssociateSubject();
    seedAssociateTeacher();
    seedAssociateClassroom();
    seedAssociateClass();
  }

  private void seedAssociateSubject() {
    if (seedSubjectName != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<Subject> subjects =
          dhi.getListSelectFromWhereEqual(Subject.class, Subject_.name, seedSubjectName);
      if (subjects != null) {
        setSubject(subjects.get(0));
      }
    }
  }

  private void seedAssociateTeacher() {
    if (seedTeacherEmail != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTeacherEmail);
      if (users != null) {
        setTeacher(users.get(0));
      }
    }
  }

  private void seedAssociateClassroom() {
    if (seedClassroomName != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<Classroom> classrooms =
          dhi.getListSelectFromWhereEqual(Classroom.class, Classroom_.name, seedClassroomName);
      if (classrooms != null) {
        setClassroom(classrooms.get(0));
      }
    }
  }

  private void seedAssociateClass() {
    if (seedClassName != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<Class> classes =
          dhi.getListSelectFromWhereEqual(Class.class, Class_.name, seedClassName);
      if (classes != null) {
        setClass_(classes.get(0));
      }
    }
  }
}

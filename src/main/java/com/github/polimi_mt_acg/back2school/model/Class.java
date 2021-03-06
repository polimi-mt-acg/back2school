package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.*;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "class",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"academic_year", "name"})
    })
public class Class implements DeserializeToPersistInterface {

  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  @Transient public List<String> seedStudentsEmail = new ArrayList<>();

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @Column(name = "academic_year")
  private int academicYear;

  @Column(name = "name")
  private String name;

  @ManyToMany
  @JoinTable(
      name = "class_user",
      joinColumns = @JoinColumn(name = "class_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
  @Fetch(FetchMode.JOIN)
  private List<User> classStudents = new ArrayList<>();

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(int academicYear) {
    this.academicYear = academicYear;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<User> getClassStudents() {
    return classStudents;
  }

  public void setClassStudents(List<User> classStudents) {
    this.classStudents = classStudents;
  }

  @JsonIgnore
  public List<String> getSeedStudentsEmail() {
    return seedStudentsEmail;
  }

  @JsonProperty
  public void setSeedStudentsEmail(List<String> seedStudentsEmail) {
    this.seedStudentsEmail = seedStudentsEmail;
  }

  public void addStudent(User student) {
    this.classStudents.add(student);
  }

  @Override
  public void prepareToPersist() {
    seedAssociateStudents();
  }

  private void seedAssociateStudents() {
    if (seedStudentsEmail.size() != 0) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      for (String seedStudentEmail : this.seedStudentsEmail) {
        List<User> users =
            dhi.getListSelectFromWhereEqual(User.class, User_.email, seedStudentEmail);
        if (users != null && users.size() >= 1) {
          addStudent(users.get(0));
        } else {
          LOGGER.info("STUDENT NOT FOUND. Skipped student with email: " + seedStudentEmail);
        }
      }
    }
  }
}

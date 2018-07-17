package com.github.polimi_mt_acg.back2school.api.v1.classes;

import java.util.ArrayList;
import java.util.List;

public class ClassStudentsResponse {
  private List<Student> students = new ArrayList<>();

  public List<Student> getStudents() {
    return students;
  }

  public void setStudents(List<Student> students) {
    this.students = students;
  }

  public static class Student {
    private String name;
    private String surname;
    private String email;
    private String url;

    public Student(String name, String surname, String email, String url) {
      this.name = name;
      this.surname = surname;
      this.email = email;
      this.url = url;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getSurname() {
      return surname;
    }

    public void setSurname(String surname) {
      this.surname = surname;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

  }
}

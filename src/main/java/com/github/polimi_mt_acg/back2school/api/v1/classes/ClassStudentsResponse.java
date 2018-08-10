package com.github.polimi_mt_acg.back2school.api.v1.classes;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ClassStudentsResponse {
  private List<Entity> students = new ArrayList<>();

  public List<Entity> getStudents() {
    return students;
  }

  public void setStudents(List<Entity> students) {
    this.students = students;
  }

  public static class Entity {
    private String name;
    private String surname;
    private String email;
    private URI url;

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

    public URI getUrl() {
      return url;
    }

    public void setUrl(URI url) {
      this.url = url;
    }
  }
}

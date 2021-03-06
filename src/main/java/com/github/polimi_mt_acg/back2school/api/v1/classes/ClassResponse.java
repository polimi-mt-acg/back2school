package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ClassResponse {

  private String name;

  @JsonProperty("academic_year")
  private int academicYear;

  private List<Entity> students = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(int academicYear) {
    this.academicYear = academicYear;
  }

  public List<Entity> getStudents() {
    return students;
  }

  public void setStudents(List<Entity> students) {
    this.students = students;
  }

  public static class Entity extends ClassStudentsResponse.Entity {}
}

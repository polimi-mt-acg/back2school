package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ClassRequest {

  private String name;

  @JsonProperty("academic_year")
  private Integer academicYear;

  @JsonProperty("students_ids")
  private List<Integer> studentsIds = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(Integer academicYear) {
    this.academicYear = academicYear;
  }

  public List<Integer> getStudentsIds() {
    return studentsIds;
  }

  public void setStudentsIds(List<Integer> studentsIds) {
    this.studentsIds = studentsIds;
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class StudentGradeRequest {

  @JsonProperty("subject_id")
  private Integer subjectId;
  private LocalDate date;
  private String title;
  private double grade;

  public Integer getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Integer subjectId) {
    this.subjectId = subjectId;
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

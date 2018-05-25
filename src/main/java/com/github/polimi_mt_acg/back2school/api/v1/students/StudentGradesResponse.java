package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class StudentGradesResponse {
  private List<URI> grades;

  @JsonProperty public List<URI> getGrades() {
    return grades;
  }

  @JsonProperty public void setGrades(List<URI> grades) {
    this.grades = grades;
  }
}

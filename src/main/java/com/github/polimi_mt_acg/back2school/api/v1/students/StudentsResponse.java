package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class StudentsResponse {
  private List<URI> students;

  @JsonProperty public List<URI> getStudents() {
    return students;
  }

  @JsonProperty public void setStudents(List<URI> students) {
    this.students = students;
  }
}

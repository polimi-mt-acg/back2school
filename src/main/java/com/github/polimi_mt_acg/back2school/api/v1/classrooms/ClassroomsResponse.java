package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class ClassroomsResponse {
  private List<URI> classrooms;

  @JsonProperty public List<URI> getClassrooms() {
    return classrooms;
  }

  @JsonProperty public void setClassrooms(List<URI> students) {
    this.classrooms = classrooms;
  }
}

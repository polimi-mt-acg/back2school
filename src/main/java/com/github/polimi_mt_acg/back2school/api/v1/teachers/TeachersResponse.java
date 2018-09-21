package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class TeachersResponse {

  @JsonProperty private List<URI> teachers;

  public List<URI> getTeachers() {
    return teachers;
  }

  public void setTeachers(List<URI> teachers) {
    this.teachers = teachers;
  }
}

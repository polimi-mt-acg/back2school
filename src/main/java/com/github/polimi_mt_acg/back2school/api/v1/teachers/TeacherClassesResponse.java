package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class TeacherClassesResponse {
  private List<ClassResponse> classes;

  @JsonProperty
  public List<ClassResponse> getClasses() {
    return classes;
  }

  @JsonProperty
  public void setClasses(List<ClassResponse> classes) {
    this.classes = classes;
  }
}

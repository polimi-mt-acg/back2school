package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class SubjectResponse {
  private List<URI> subjects;

  @JsonProperty public List<URI> getSubjects() {
    return subjects;
  }

  @JsonProperty public void setSubjects(List<URI> subjects) {
    this.subjects = subjects;
  }
}

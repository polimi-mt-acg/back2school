package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class ParentsResponse {
  private List<URI> parents;

  @JsonProperty public List<URI> getParents() {
    return parents;
  }

  @JsonProperty public void setParents(List<URI> parents) {
    this.parents = parents;
  }
}

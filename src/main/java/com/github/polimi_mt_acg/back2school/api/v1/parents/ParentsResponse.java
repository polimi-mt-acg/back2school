package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class ParentsResponse {

  @JsonProperty private List<URI> parents;

  public List<URI> getParents() {
    return parents;
  }

  public void setParents(List<URI> parents) {
    this.parents = parents;
  }
}

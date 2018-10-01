package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class ParentChildrenResponse {

  @JsonProperty
  private List<URI> children;

  public List<URI> getChildren() {
    return children;
  }

  public void setChildren(List<URI> children) {
    this.children = children;
  }
}

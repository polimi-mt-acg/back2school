package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParentsChildrenRequest {

  @JsonProperty("child_id")
  private Integer childId;

  public Integer getChildId() {
    return childId;
  }

  public void setChildId(Integer childId) {
    this.childId = childId;
  }
}

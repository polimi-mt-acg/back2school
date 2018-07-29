package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.model.User;

import javax.jws.soap.SOAPBinding;
import java.net.URI;
import java.util.List;

public class ParentChildrenResponse {
  private List<User> children;

  @JsonProperty public List<User> getChildren() {
    return children;
  }

  @JsonProperty public void setChildren(List<User> children) {
    this.children = children;
  }
}

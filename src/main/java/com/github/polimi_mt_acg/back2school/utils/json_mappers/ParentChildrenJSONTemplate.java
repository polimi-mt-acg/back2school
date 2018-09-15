package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ParentChildrenJSONTemplate implements JSONTemplateInterface {

  @JsonProperty("parent_children")
  public List<SeedEntityParentChild> parentChildren;

  @Override
  public List<?> getEntities() {
    return parentChildren;
  }
}

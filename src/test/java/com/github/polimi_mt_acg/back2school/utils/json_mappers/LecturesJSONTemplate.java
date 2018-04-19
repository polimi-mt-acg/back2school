package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Lecture;
import java.util.List;

public class LecturesJSONTemplate implements JSONTemplateInterface {

  public List<Lecture> lectures;

  @Override
  public List<?> getEntities() {
    return lectures;
  }
}

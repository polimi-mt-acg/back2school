package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Subject;
import java.util.List;

public class SubjectsJSONTemplate implements JSONTemplateInterface {

  public List<Subject> subjects;

  @Override
  public List<?> getEntities() {
    return subjects;
  }
}

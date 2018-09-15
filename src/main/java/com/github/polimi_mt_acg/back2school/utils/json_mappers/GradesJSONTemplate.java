package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Grade;
import java.util.List;

public class GradesJSONTemplate implements JSONTemplateInterface {

  public List<Grade> grades;

  @Override
  public List<?> getEntities() {
    return grades;
  }
}

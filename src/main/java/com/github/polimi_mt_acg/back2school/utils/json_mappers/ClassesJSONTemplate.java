package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Class;
import java.util.List;

public class ClassesJSONTemplate implements JSONTemplateInterface {

  public List<Class> classes;

  @Override
  public List<?> getEntities() {
    return classes;
  }
}

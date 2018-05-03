package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationGeneralTeachers;
import java.util.List;

public class NotificationsGeneralTeachersJSONTemplate implements JSONTemplateInterface {

  public List<NotificationGeneralTeachers> notificationsGeneralTeachers;

  @Override
  public List<?> getEntities() {
    return notificationsGeneralTeachers;
  }
}

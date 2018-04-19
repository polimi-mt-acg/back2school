package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationClassTeacher;
import java.util.List;

public class NotificationsClassTeacherJSONTemplate implements JSONTemplateInterface {

  public List<NotificationClassTeacher> notificationsClassTeacher;

  @Override
  public List<?> getEntities() {
    return notificationsClassTeacher;
  }
}

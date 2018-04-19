package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationPersonalTeacher;
import java.util.List;

public class NotificationsPersonalTeacherJSONTemplate implements JSONTemplateInterface {

  public List<NotificationPersonalTeacher> notificationsPersonalTeacher;

  @Override
  public List<?> getEntities() {
    return notificationsPersonalTeacher;
  }
}

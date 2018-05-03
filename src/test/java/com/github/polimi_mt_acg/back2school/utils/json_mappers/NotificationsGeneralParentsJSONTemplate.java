package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationGeneralParents;
import java.util.List;

public class NotificationsGeneralParentsJSONTemplate implements JSONTemplateInterface {

  public List<NotificationGeneralParents> notificationsGeneralParents;

  @Override
  public List<?> getEntities() {
    return notificationsGeneralParents;
  }
}

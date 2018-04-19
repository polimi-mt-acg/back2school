package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationClassParent;
import java.util.List;

public class NotificationsClassParentJSONTemplate implements JSONTemplateInterface {

  public List<NotificationClassParent> notificationsClassParent;

  @Override
  public List<?> getEntities() {
    return notificationsClassParent;
  }
}
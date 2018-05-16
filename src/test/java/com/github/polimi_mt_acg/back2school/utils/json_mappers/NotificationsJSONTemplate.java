package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Notification;
import java.util.List;

public class NotificationsJSONTemplate implements JSONTemplateInterface {

  public List<Notification> notifications;

  @Override
  public List<?> getEntities() {
    return notifications;
  }
}

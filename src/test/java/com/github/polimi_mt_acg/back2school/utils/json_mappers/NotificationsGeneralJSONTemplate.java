package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.NotificationGeneral;
import java.util.List;

public class NotificationsGeneralJSONTemplate implements JSONTemplateInterface {

  public List<NotificationGeneral> notificationsGeneral;

  @Override
  public List<?> getEntities() {
    return notificationsGeneral;
  }
}

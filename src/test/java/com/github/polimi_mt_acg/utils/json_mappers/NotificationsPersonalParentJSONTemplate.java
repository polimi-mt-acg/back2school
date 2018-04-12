package com.github.polimi_mt_acg.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Appointment;
import com.github.polimi_mt_acg.back2school.model.NotificationPersonalParent;

import java.util.List;

public class NotificationsPersonalParentJSONTemplate implements JSONTemplateInterface {

    public List<NotificationPersonalParent> notificationsPersonalParent;

    @Override
    public List<?> getEntities() {
        return notificationsPersonalParent;
    }
}

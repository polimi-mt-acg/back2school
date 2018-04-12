package com.github.polimi_mt_acg.utils.json_mappers;

import java.util.List;

public class NotificationsReadJSONTemplate implements JSONTemplateInterface {

    public List<SeedEntityNotificationRead> notificationsRead;

    @Override
    public List<?> getEntities() {
        return notificationsRead;
    }
}

package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "notification_general")
@DiscriminatorValue(value = "general")
public class NotificationGeneral
        extends Notification implements DeserializeToPersistInterface {

    @Override
    public void prepareToPersist() {

    }
}

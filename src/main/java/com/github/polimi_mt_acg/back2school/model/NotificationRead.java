package com.github.polimi_mt_acg.back2school.model;

public class NotificationRead implements DeserializeToPersistInterface {

    private int notification_id;
    private int user_id;

    @Override
    public void prepareToPersist() {

    }
}

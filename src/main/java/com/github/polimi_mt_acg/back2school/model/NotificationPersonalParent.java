package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_personal_parent")
public class NotificationPersonalParent
        extends Notification implements DeserializeToPersistInterface {

    // Reference to the single user since the notification is personal
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_USER_PARENT_ID_FK"))
    private User target;

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    @Override
    public void prepareToPersist() {

    }
}

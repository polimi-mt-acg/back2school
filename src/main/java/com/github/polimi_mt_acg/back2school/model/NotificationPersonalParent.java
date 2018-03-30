package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_personal_parent")
@DiscriminatorValue(value = "personal_parent")
public class NotificationPersonalParent extends Notification {
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_USER_PARENT_ID_FK"))
    private User targetup;


    public User getTarget() {
        return targetup;
    }

    public void setTarget(User target) {
        this.targetup = target;
    }
}

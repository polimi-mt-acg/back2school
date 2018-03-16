package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity
@Table(name = "notification_class")
public class NotificationClass extends Notification {
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_ID_FK"))
    private int targetId;
    //usato per indicare la classe (se il tipo e' classe)

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
}

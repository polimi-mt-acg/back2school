package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity
@Table(name = "notification_personal")
public class NotificationPersonal extends Notification{
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_USER_ID_FK"))
    private int targetId;
    //usato per indicare il singolo utente (se il tipo è personal)


    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }
}

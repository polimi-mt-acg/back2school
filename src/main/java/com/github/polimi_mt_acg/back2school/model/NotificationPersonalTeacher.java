package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_personal_teacher")
public class NotificationPersonalTeacher extends Notification {
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_USER_TEACHER_ID_FK"))
    private User target;
    //usato per indicare il singolo utente (se il tipo è personal)

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }
}
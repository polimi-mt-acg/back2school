package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity//(name = "notification_personal_teacher")
@DiscriminatorValue(value = "personal_teacher")
public class NotificationPersonalTeacher extends Notification {
    @OneToOne
    @JoinColumn(name = "target_personal_teacher_id",
            foreignKey = @ForeignKey(name = "TARGET_USER_TEACHER_ID_FK"))
    private User targetut;
    //usato per indicare il singolo utente (se il tipo è personal)

    public User getTarget() {
        return targetut;
    }

    public void setTarget(User target) {
        this.targetut = target;
    }
}

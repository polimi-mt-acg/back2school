package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "PERSONAL-TEACHER")
public class NotificationPersonalTeacher extends Notification {

    @OneToOne
    @JoinColumn(name = "target_user_id",
            foreignKey = @ForeignKey(name = "TARGET_PERSONAL_TEACHER_ID_FK"))
    private User targetUser;

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public void prepareToPersist() {
        super.prepareToPersist();
    }
}

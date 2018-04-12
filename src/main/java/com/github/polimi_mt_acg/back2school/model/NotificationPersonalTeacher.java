package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue(value = "PERSONAL-TEACHER")
public class NotificationPersonalTeacher extends Notification {

    @OneToOne
    @JoinColumn(name = "target_user_id",
            foreignKey = @ForeignKey(name = "TARGET_PERSONAL_TEACHER_ID_FK"))
    private User targetUser;

    @Transient
    public String seedTargetTeacherEmail;

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public void prepareToPersist() {
        super.prepareToPersist();
        seedAssociateTargetTeacher();
    }

    private void seedAssociateTargetTeacher() {
        if (seedTargetTeacherEmail != null) {
            DatabaseHandler dhi = DatabaseHandler.getInstance();
            List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTargetTeacherEmail);
            if (users != null) {
                setTargetUser(users.get(0));
            }
        }
    }
}

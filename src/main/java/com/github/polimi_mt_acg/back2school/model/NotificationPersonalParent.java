package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue(value = "PERSONAL-PARENT")
public class NotificationPersonalParent extends Notification {

    @OneToOne
    @JoinColumn(name = "target_user_id",
            foreignKey = @ForeignKey(name = "TARGET_PERSONAL_PARENT_ID_FK"))
    private User targetUser;

    @Transient
    public String seedTargetParentEmail;

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public void prepareToPersist() {
        super.prepareToPersist();
        seedAssociateTargetParent();
    }

    private void seedAssociateTargetParent() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTargetParentEmail);
        if (users != null) {
            setTargetUser(users.get(0));
        }
    }
}

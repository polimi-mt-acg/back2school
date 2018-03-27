package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_class_teacher")
public class NotificationClassTeacher extends Notification {
    //@OneToOne
    //@JoinColumn(name = "target_id",
    // foreignKey = @ForeignKey(name = "TARGET_CLASS_TEACHERS_ID_FK"))
    private SchoolClass target;

    public SchoolClass getTarget() {
        return target;
    }

    public void setTarget(SchoolClass target) {
        this.target = target;
    }
}

package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity//(name = "notification_class_parent")
@DiscriminatorValue("notification_class_parent")
public class NotificationClassParent extends Notification {
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_PARENTS_ID_FK"))
    private SchoolClass target ;//= new SchoolClass();

    public SchoolClass getTarget() { return target; }

    public void setTarget(SchoolClass target) { this.target = target; }
}

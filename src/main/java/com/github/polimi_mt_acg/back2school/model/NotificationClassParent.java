package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_class_parent")
@DiscriminatorValue(value = "CLASS_PARENT")
public class NotificationClassParent
        extends Notification implements DeserializeToPersistInterface {

    
    @OneToOne
    @JoinColumn(name = "target_class_parents_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_PARENTS_ID_FK"))
    private SchoolClass targetcp ;//= new SchoolClass();

    public SchoolClass getTarget() { return targetcp; }

    public void setTarget(Class target) {
        this.target = target;
    }

    @Override
    public void prepareToPersist() {

    }
}

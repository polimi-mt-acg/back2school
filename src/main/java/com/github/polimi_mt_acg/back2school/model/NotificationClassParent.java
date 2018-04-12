package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "CLASS-PARENT")
public class NotificationClassParent extends Notification {

    @OneToOne
    @JoinColumn(name = "target_class_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_PARENTS_ID_FK"))
    private Class targetClass;

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void prepareToPersist() {
        super.prepareToPersist();
    }
}

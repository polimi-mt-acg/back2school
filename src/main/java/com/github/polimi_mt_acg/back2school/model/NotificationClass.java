package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity(name = "notification_class")
public class NotificationClass
        extends Notification implements DeserializeToPersistInterface {

    //usato per indicare la classe (se il tipo e' classe)
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_ID_FK"))
    private Class target;

    public Class getTarget() {
        return target;
    }

    public void setTarget(Class target) {
        this.target = target;
    }

    @Override
    public void prepareToPersist() {

    }
}

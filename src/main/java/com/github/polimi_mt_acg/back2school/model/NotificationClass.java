package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.*;

@Entity
@Table(name = "notification_class")
public class NotificationClass extends Notification {
    @OneToOne
    @JoinColumn(name = "target_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_ID_FK"))
    private Class target;
    //usato per indicare la classe (se il tipo e' classe)

    public Class getTarget() {
        return target;
    }

    public void setTarget(Class target) {
        this.target = target;
    }
}

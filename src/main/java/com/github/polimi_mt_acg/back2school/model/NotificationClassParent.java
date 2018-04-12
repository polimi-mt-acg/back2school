package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue(value = "CLASS-PARENT")
public class NotificationClassParent extends Notification {

    @OneToOne
    @JoinColumn(name = "target_class_id",
            foreignKey = @ForeignKey(name = "TARGET_CLASS_PARENTS_ID_FK"))
    private Class targetClass;

    @Transient
    public String seedTargetClassName;

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public void prepareToPersist() {
        super.prepareToPersist();
        seedAssociateTargetClass();
    }

    private void seedAssociateTargetClass() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<Class> classes = dhi.getListSelectFromWhereEqual(Class.class, Class_.name, seedTargetClassName);
        if (classes != null) {
            setTargetClass(classes.get(0));
        }
    }
}

package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "CLASS-TEACHER")
public class NotificationClassTeacher extends Notification {

  @Transient
  public String seedTargetClassName;
  @OneToOne
  @JoinColumn(
      name = "target_class_id",
      foreignKey = @ForeignKey(name = "TARGET_CLASS_TEACHER_ID_FK")
  )
  private Class targetClass;

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
    if (seedTargetClassName != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<Class> classes =
          dhi.getListSelectFromWhereEqual(Class.class, Class_.name, seedTargetClassName);
      if (classes != null) {
        setTargetClass(classes.get(0));
      }
    }
  }
}

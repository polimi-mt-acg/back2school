package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "CLASS-PARENT")
public class NotificationClassParent extends Notification {

  @Transient private String seedTargetClassName;

  @OneToOne
  @JoinColumn(
    name = "target_class_id",
    foreignKey = @ForeignKey(name = "TARGET_CLASS_PARENTS_ID_FK")
  )
  private Class targetClass;

  @JsonProperty public Class getTargetClass() {
    return targetClass;
  }

  @JsonProperty public void setTargetClass(Class targetClass) {
    this.targetClass = targetClass;
  }

  @JsonIgnore
  public String getSeedTargetClassName() {
    return seedTargetClassName;
  }

  @JsonProperty
  public void setSeedTargetClassName(String seedTargetClassName) {
    this.seedTargetClassName = seedTargetClassName;
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

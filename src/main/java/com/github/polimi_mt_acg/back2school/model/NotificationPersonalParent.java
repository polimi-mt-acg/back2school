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
@DiscriminatorValue(value = "PERSONAL-PARENT")
public class NotificationPersonalParent extends Notification {

  @Transient @JsonIgnore public String seedTargetParentEmail;

  @OneToOne
  @JoinColumn(
    name = "target_user_id",
    foreignKey = @ForeignKey(name = "TARGET_PERSONAL_PARENT_ID_FK")
  )
  private User targetUser;

  @JsonProperty
  public User getTargetUser() {
    return targetUser;
  }

  @JsonProperty
  public void setTargetUser(User targetUser) {
    this.targetUser = targetUser;
  }

  public void prepareToPersist() {
    super.prepareToPersist();
    seedAssociateTargetParent();
  }

  private void seedAssociateTargetParent() {
    if (seedTargetParentEmail != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<User> users =
          dhi.getListSelectFromWhereEqual(User.class, User_.email, seedTargetParentEmail);
      if (users != null) {
        setTargetUser(users.get(0));
      }
    }
  }
}

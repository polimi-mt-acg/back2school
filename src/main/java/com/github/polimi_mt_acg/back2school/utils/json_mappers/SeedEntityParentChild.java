package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.SeedDummy;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;

@Entity
public class SeedEntityParentChild extends SeedDummy {

  @Transient public String seedParentEmail;

  @Transient public String seedChildEmail;

  @Override
  public void prepareToPersist() {
    super.prepareToPersist();
    seedAssociateChildToParent();
  }

  private void seedAssociateChildToParent() {
    Session session = DatabaseHandler.getInstance().getNewSession();

    session.beginTransaction();

    Optional<User> parentOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, seedParentEmail, session);

    Optional<User> childOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, seedChildEmail, session);

    // if the parent is not found into the database
    if (!parentOpt.isPresent()) {
      print(
          "WARNING! parent_child.json found seedParentEmail: ",
          seedParentEmail,
          " BUT there IS NO corresponding user in the database.");
      print("PARENT-CHILD relation NOT created!");
      return;
    }

    // if the child is not found into the database
    if (!childOpt.isPresent()) {
      print(
          "WARNING! parent_child.json found seedChildEmail: ",
          seedChildEmail,
          " BUT there IS NO corresponding user in the database.");
      print("PARENT-CHILD relation NOT created!");
      return;
    }

    User parent = parentOpt.get();
    User child = childOpt.get();

    if (!parent.getRole().equals(User.Role.PARENT)) {
      print(
          "WARNING! parent_child.json found seedParentEmail: ",
          seedParentEmail,
          " with role: ",
          parent.getRole(),
          " INSTEAD of PARENT.");
      print("PARENT-CHILD relation NOT created!");
      return;
    }
    if (!child.getRole().equals(User.Role.STUDENT)) {
      print(
          "WARNING! parent_child.json found seedChildEmail: ",
          seedChildEmail,
          " with role: ",
          child.getRole(),
          " INSTEAD of STUDENT.");
      print("PARENT-CHILD relation NOT created!");
      return;
    }

    parent.addChild(child);

    session.getTransaction().commit();
    session.close();
  }
}

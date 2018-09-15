package com.github.polimi_mt_acg.back2school;

import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.JacksonCustomMapper;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.hibernate.Session;

import java.util.Optional;

@ApplicationPath("api/v1")
public class RESTApplication extends ResourceConfig {

  public RESTApplication() {
    packages("com.github.polimi_mt_acg.back2school");
    register(JacksonCustomMapper.class);
    register(JacksonFeature.class);

    ensureAdminUserPresent();
  }

  private void ensureAdminUserPresent() {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Optional<User> optAdminUser = DatabaseHandler.fetchEntityBy(User.class, User_.role, User.Role.ADMINISTRATOR, session);

    if (!optAdminUser.isPresent()) {
      // create new admin user
      User defaultAdminUser = new User();
      defaultAdminUser.setRole(User.Role.ADMINISTRATOR);
      defaultAdminUser.setName("Admin");
      defaultAdminUser.setSurname("Admin surname");
      defaultAdminUser.setEmail("admin@email.com");
      defaultAdminUser.setPassword("admin");

      // save the created user
      session.persist(defaultAdminUser);
    }

    session.getTransaction().commit();
    session.close();
  }
}

package com.github.polimi_mt_acg.back2school.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;

import java.util.Optional;

import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ParentChildrenTest {

  @BeforeClass
  public static void oneTimeSetUp() {
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");
  }

  @AfterClass
  public static void oneTimeTearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testBobChildren() {
    Session session = DatabaseHandler.getInstance().getNewSession();
    Optional<User> bobOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "bob@email.com", session);

    assertTrue(bobOpt.isPresent());
    User bob = bobOpt.get();

    assertEquals(2, bob.getChildren().size());
    session.close();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testBob2Children() {
    Session session = DatabaseHandler.getInstance().getNewSession();
    Optional<User> bob2Opt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, "bob2@email.com", session);

    assertTrue(bob2Opt.isPresent());
    User bob2 = bob2Opt.get();

    assertEquals(1, bob2.getChildren().size());
    session.close();
  }
}

package com.github.polimi_mt_acg.back2school.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.polimi_mt_acg.back2school.model.User;
import java.util.List;
import javax.persistence.criteria.CriteriaQuery;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class DatabaseHandlerTest {

  private static DatabaseHandler dbh;

  @BeforeClass
  public static void setUpClass() {
    dbh = DatabaseHandler.getInstance();
  }

  @AfterClass
  public static void tearDownClass() {
    dbh.truncateDatabase();
    dbh.destroy();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void getInstance() {
    DatabaseHandler db = DatabaseHandler.getInstance();
    assertNotNull(db);
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void getNewSession() {
    Session session = dbh.getNewSession();
    assertNotNull(session);
    session.close();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void getCriteriaQuery() {
    CriteriaQuery<User> criteria = dbh.getCriteriaQuery(User.class);
    assertNotNull(criteria);
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void getResultListSelectFrom() {
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");

    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "users.json");

    List<User> users = dbh.getListSelectFrom(User.class);

    assertNotNull(users);
    assertNotNull(seedUsers);
    assertEquals(seedUsers.size(), users.size());
    assertEquals(seedUsers.get(0).getName(), users.get(0).getName());
    assertEquals(seedUsers.get(1).getName(), users.get(1).getName());
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void truncateDatabase() {
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");
    dbh.truncateDatabase();

    List<User> users = dbh.getListSelectFrom(User.class);
    assertNotNull(users);
    assertEquals(0, users.size());
  }
}

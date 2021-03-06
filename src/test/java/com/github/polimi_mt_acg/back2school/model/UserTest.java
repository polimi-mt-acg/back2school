package com.github.polimi_mt_acg.back2school.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class UserTest {

  @BeforeClass
  public static void setUpClass() {
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");
  }

  @AfterClass
  public static void tearDownClass() {
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testUserEntity() {
    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "users.json");

    assertNotNull(seedUsers);
    assertTrue(seedUsers.size() >= 1);

    User seedEntity = seedUsers.get(0);
    // get user from database
    User databaseEntity = DatabaseHandler.getInstance().getListSelectFrom(User.class).get(0);

    assertNotNull(databaseEntity);
    assertEquals(seedEntity.getRole(), databaseEntity.getRole());
    assertEquals(seedEntity.getName(), databaseEntity.getName());
    assertEquals(seedEntity.getSurname(), databaseEntity.getSurname());
    assertEquals(seedEntity.getEmail(), databaseEntity.getEmail());
    assertNotNull(databaseEntity.getSalt());
    assertNotNull(databaseEntity.getPassword());
  }
}

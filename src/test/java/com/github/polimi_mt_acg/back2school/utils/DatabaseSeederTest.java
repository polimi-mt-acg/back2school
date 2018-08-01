package com.github.polimi_mt_acg.back2school.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.polimi_mt_acg.back2school.model.User;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class DatabaseSeederTest {

  @Test
  @Category(TestCategory.Unit.class)
  public void deployScenario() {
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void getEntitiesListFromSeed() {
    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "users.json");

    String name = "Alice";
    String surname = "SurnameAlice";
    String email = "alice@email.com";

    assertNotNull(seedUsers);

    User loadedUser = seedUsers.get(0);

    assertNotNull(loadedUser);
    assertEquals(loadedUser.getName(), name);
    assertEquals(loadedUser.getSurname(), surname);
    assertEquals(loadedUser.getEmail(), email);
  }
}

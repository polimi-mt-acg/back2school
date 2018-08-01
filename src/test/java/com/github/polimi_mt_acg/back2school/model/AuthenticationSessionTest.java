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

public class AuthenticationSessionTest {

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
  public void testAuthenticationSessionEntity() {
    List<AuthenticationSession> seedAuthenticationSessions =
        (List<AuthenticationSession>)
            DatabaseSeeder.getEntitiesListFromSeed(
                "scenarioA_unit_tests", "authentication_sessions.json");

    assertNotNull(seedAuthenticationSessions);
    assertEquals(seedAuthenticationSessions.size(), 1);

    AuthenticationSession seedEntity = seedAuthenticationSessions.get(0);
    // get entity from database
    AuthenticationSession databaseEntity =
        DatabaseHandler.getInstance().getListSelectFrom(AuthenticationSession.class).get(0);

    // asserts beginning
    assertNotNull(databaseEntity);
    assertNotNull(databaseEntity.getUser());
    assertEquals(seedEntity.seedUserEmail, databaseEntity.getUser().getEmail());
    assertNotNull(databaseEntity.getDatetimeLastInteraction());
    assertTrue(!databaseEntity.isCancelled());
  }
}

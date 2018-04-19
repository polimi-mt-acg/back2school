package com.github.polimi_mt_acg.back2school.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ClassroomTest {

  @BeforeClass
  public static void setUpClass() {
    DatabaseSeeder.deployScenario("scenarioA_unit_tests");
  }

  @AfterClass
  public static void tearDownClass() {
    DatabaseHandler.getInstance().truncateDatabase();
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testClassroomEntity() {
    List<Classroom> seedClassrooms =
        (List<Classroom>)
            DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "classrooms.json");

    assertNotNull(seedClassrooms);
    assertEquals(seedClassrooms.size(), 1);

    Classroom seedEntity = seedClassrooms.get(0);
    // get entity from database
    Classroom databaseEntity =
        DatabaseHandler.getInstance().getListSelectFrom(Classroom.class).get(0);

    assertNotNull(databaseEntity);
    assertEquals(seedEntity.getName(), databaseEntity.getName());
    assertEquals(seedEntity.getFloor(), databaseEntity.getFloor());
    assertEquals(seedEntity.getBuilding(), databaseEntity.getBuilding());
  }
}

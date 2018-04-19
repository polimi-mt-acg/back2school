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

public class ClassTest {

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
  public void testClassEntity() {
    List<Class> seedClasses =
        (List<Class>)
            DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "classes.json");

    // get entity from database
    Class databaseEntity = DatabaseHandler.getInstance().getListSelectFrom(Class.class).get(0);

    // asserts beginning
    assertNotNull(seedClasses);
    assertEquals(1, seedClasses.size());
    Class seedEntity = seedClasses.get(0);

    assertNotNull(databaseEntity);

    assertEquals(seedEntity.getAcademicYear(), databaseEntity.getAcademicYear());
    assertEquals(seedEntity.getName(), databaseEntity.getName());
    assertEquals(1, databaseEntity.getClassStudents().size());
    assertEquals(1, seedEntity.seedStudentsEmail.size());

    User databaseStudent = databaseEntity.getClassStudents().get(0);
    assertEquals(databaseStudent.getEmail(), seedEntity.seedStudentsEmail.get(0));
  }
}

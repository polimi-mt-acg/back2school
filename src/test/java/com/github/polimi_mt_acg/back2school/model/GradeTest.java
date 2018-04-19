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

public class GradeTest {

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
  public void testGradeEntity() {
    List<Grade> seedGrades =
        (List<Grade>) DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "grades.json");

    assertNotNull(seedGrades);
    assertEquals(seedGrades.size(), 1);

    Grade seedEntity = seedGrades.get(0);
    // get entity from database
    Grade databaseEntity = DatabaseHandler.getInstance().getListSelectFrom(Grade.class).get(0);

    // asserts beginning
    assertNotNull(databaseEntity);
    assertNotNull(databaseEntity.getSubject());
    assertEquals(seedEntity.seedSubjectName, databaseEntity.getSubject().getName());
    assertNotNull(databaseEntity.getTeacher());
    assertEquals(seedEntity.seedTeacherEmail, databaseEntity.getTeacher().getEmail());
    assertNotNull(databaseEntity.getStudent());
    assertEquals(seedEntity.seedStudentEmail, databaseEntity.getStudent().getEmail());
    assertEquals(seedEntity.getDate().toString(), databaseEntity.getDate().toString());
    assertEquals(seedEntity.getTitle(), databaseEntity.getTitle());
    assertEquals(seedEntity.getGrade(), databaseEntity.getGrade(), 0.001);
  }
}

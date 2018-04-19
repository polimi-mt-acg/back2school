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

public class LectureTest {

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
  public void testLectureEntity() {
    List<Lecture> seedLectures =
        (List<Lecture>)
            DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "lectures.json");

    assertNotNull(seedLectures);
    assertEquals(seedLectures.size(), 1);

    Lecture seedEntity = seedLectures.get(0);
    // get entity from database
    Lecture databaseEntity = DatabaseHandler.getInstance().getListSelectFrom(Lecture.class).get(0);

    // asserts beginning
    assertNotNull(databaseEntity);
    assertNotNull(databaseEntity.getSubject());
    assertNotNull(databaseEntity.getTeacher());
    assertNotNull(databaseEntity.getClassroom());
    assertNotNull(databaseEntity.getClass_());

    assertEquals(seedEntity.seedSubjectName, databaseEntity.getSubject().getName());
    assertEquals(seedEntity.seedSubjectName, databaseEntity.getSubject().getName());
    assertNotNull(databaseEntity.getTeacher());
    assertEquals(seedEntity.seedTeacherEmail, databaseEntity.getTeacher().getEmail());
    assertEquals(seedEntity.seedClassroomName, databaseEntity.getClassroom().getName());
    assertEquals(seedEntity.seedClassName, databaseEntity.getClass_().getName());

    assertEquals(
        seedEntity.getDatetimeStart().toString(), databaseEntity.getDatetimeStart().toString());
    assertEquals(
        seedEntity.getDatetimeEnd().toString(), databaseEntity.getDatetimeEnd().toString());
  }
}

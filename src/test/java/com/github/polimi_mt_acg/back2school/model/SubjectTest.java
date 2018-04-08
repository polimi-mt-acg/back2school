package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.*;

public class SubjectTest {

    @BeforeClass
    public static void setUpClass() {
        DatabaseSeeder.deployScenario("scenarioA_unit_tests");
    }

    @AfterClass
    public static void tearDownClass() {
        DatabaseHandler.getInstance().truncateDatabase();
    }

    @Test
    @Category(TestCategory.Transient.class)
    public void testSubjectEntity() {
        List<Subject> seedSubjects = (List<Subject>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "subjects.json");

        assertNotNull(seedSubjects);
        assertEquals(seedSubjects.size(), 1);

        Subject seedEntity = seedSubjects.get(0);
        // get entity from database
        Subject databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(Subject.class).get(0);

        assertNotNull(databaseEntity);
        assertEquals(seedEntity.getName(), databaseEntity.getName());
        assertEquals(seedEntity.getDescription(), databaseEntity.getDescription());
    }
}

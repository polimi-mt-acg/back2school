package com.github.polimi_mt_acg.back2school;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.DatabaseSeeder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ScenarioDeploymentTest {

    @Before
    public void setUp() {
        DatabaseHandler.getInstance().truncateDatabase();
    }

    @After
    public void tearDown() {
        DatabaseHandler.getInstance().truncateDatabase();
    }

    @Test
    @Category(TestCategory.Integration.class)
    public void testScenarioDeployment() {
        DatabaseSeeder.deployScenario("scenarioA");
    }
}

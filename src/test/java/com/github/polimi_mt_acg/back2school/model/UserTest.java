package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.DatabaseSeeder;
import org.hibernate.Session;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserTest {

    Session session;

    @BeforeClass
    public static void setUpClass() {
        DatabaseSeeder.deployScenario("scenarioA_unit_tests");
    }

    @AfterClass
    public static void tearDownClass() {
        DatabaseHandler.getInstance().truncateDatabase();
    }

    @Before
    public void setUp() {
        session = DatabaseHandler.getInstance().getNewSession();
        session.beginTransaction();
    }

    @After
    public void tearDown() {
        if (session != null) {
            session.close();
        }
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testUserEntity() {
        List<User> seedUsers = (List<User>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "users.json");

        assertNotNull(seedUsers);
        assertTrue(seedUsers.size() >= 1);

        User seedEntity = seedUsers.get(0);
        // get user from database
        User databaseEntity = DatabaseHandler
                .getInstance().getResultListSelectFrom(User.class).get(0);

        assertEquals(seedEntity.getName(), databaseEntity.getName());
        assertEquals(seedEntity.getSurname(), databaseEntity.getSurname());
        assertEquals(seedEntity.getEmail(), databaseEntity.getEmail());
    }
}

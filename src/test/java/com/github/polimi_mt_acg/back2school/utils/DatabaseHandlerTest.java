package com.github.polimi_mt_acg.back2school.utils;

import com.github.polimi_mt_acg.back2school.model.User;
import org.hibernate.Session;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.persistence.criteria.CriteriaQuery;

import java.util.List;

import static org.junit.Assert.*;

public class DatabaseHandlerTest {

    private static DatabaseHandler dbh;

    @BeforeClass
    public static void setUpClass() {
        dbh = DatabaseHandler.getInstance();
    }


    @Test
    @Category(TestCategory.Unit.class)
    public void getInstance() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        assertNotNull(db);
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void getNewSession() {
        Session session = dbh.getNewSession();
        assertNotNull(session);
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void getCriteriaQuery() {
        CriteriaQuery<User> criteria = dbh.getCriteriaQuery(User.class);
        assertNotNull(criteria);
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void getResultListSelectFrom() {
        DatabaseSeeder.deployScenario("scenarioA_unit_tests");

        List<User> seedUsers = (List<User>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "users.json");

        List<User> users = dbh.getListSelectFrom(User.class);

        assertNotNull(users);
        assertNotNull(seedUsers);
        assertEquals(users.size(), seedUsers.size());
        assertEquals(users.get(0).getName(), seedUsers.get(0).getName());
        assertEquals(users.get(1).getName(), seedUsers.get(1).getName());
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void truncateDatabase() {
        DatabaseSeeder.deployScenario("scenarioA_unit_tests");
        dbh.truncateDatabase();

        List<User> users = dbh.getListSelectFrom(User.class);
        assertNotNull(users);
        assertEquals(users.size(), 0);
    }
}

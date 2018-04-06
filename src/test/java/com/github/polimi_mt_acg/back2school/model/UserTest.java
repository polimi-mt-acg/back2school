package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.DatabaseSeeder;
import org.apache.maven.shared.invoker.SystemOutHandler;
import org.hibernate.Session;
import org.junit.*;
import org.junit.experimental.categories.Category;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertEquals(seedUsers.size(), 1);

        User seedEntity = seedUsers.get(0);
        User databaseEntity = getUserFromDB(seedEntity.getEmail());


        assertEquals(seedEntity.getName(), databaseEntity.getName());
        assertEquals(seedEntity.getSurname(), databaseEntity.getSurname());
        assertEquals(seedEntity.getEmail(), databaseEntity.getEmail());
    }

    private User getUserFromDB(String email) {
        CriteriaBuilder builder = session.getCriteriaBuilder();

        CriteriaQuery<User> criteria = builder.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.select(root);
        criteria.where(builder.equal(root.get(User_.email), email));

        List<User> entities = session.createQuery(criteria).getResultList();

        assertNotNull(entities);
        assertNotNull(entities.get(0));
        return entities.get(0);
    }
}

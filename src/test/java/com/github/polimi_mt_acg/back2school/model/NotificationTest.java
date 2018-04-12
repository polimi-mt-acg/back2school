package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.*;

public class NotificationTest {

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
    public void testNotificationGeneralEntity() {
        List<NotificationGeneral> seedNotificationsGeneral = (List<NotificationGeneral>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "notifications_general.json");

        assertNotNull(seedNotificationsGeneral);
        assertEquals(seedNotificationsGeneral.size(), 1);

        NotificationGeneral seedEntity = seedNotificationsGeneral.get(0);
        // get entity from database
        NotificationGeneral databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(NotificationGeneral.class).get(0);

        // asserts beginning
        assertNotNull(databaseEntity);
        assertNotNull(databaseEntity.getCreator());
        assertEquals(
                seedEntity.seedCreatorEmail,
                databaseEntity.getCreator().getEmail()
        );
        assertEquals(
                seedEntity.getDatetime().toString(),
                databaseEntity.getDatetime().toString()
        );
        assertEquals(
                seedEntity.getSubject(),
                databaseEntity.getSubject()
        );
        assertEquals(
                seedEntity.getText(),
                databaseEntity.getText()
        );
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testNotificationPersonalParentEntity() {
        List<NotificationPersonalParent> seedNotificationsPersonalParent = (List<NotificationPersonalParent>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "notifications_personal_parent.json");

        assertNotNull(seedNotificationsPersonalParent);
        assertEquals(seedNotificationsPersonalParent.size(), 1);

        NotificationPersonalParent seedEntity = seedNotificationsPersonalParent.get(0);
        // get entity from database
        NotificationPersonalParent databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(NotificationPersonalParent.class).get(0);

        // asserts beginning
        assertNotNull(databaseEntity);
        assertNotNull(databaseEntity.getCreator());
        assertEquals(
                seedEntity.seedCreatorEmail,
                databaseEntity.getCreator().getEmail()
        );
        assertEquals(
                seedEntity.getDatetime().toString(),
                databaseEntity.getDatetime().toString()
        );
        assertEquals(
                seedEntity.getSubject(),
                databaseEntity.getSubject()
        );
        assertEquals(
                seedEntity.getText(),
                databaseEntity.getText()
        );
        assertEquals(
                seedEntity.seedTargetParentEmail,
                databaseEntity.getTargetUser().getEmail()
        );
    }
}

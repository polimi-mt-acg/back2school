package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import org.hibernate.Session;
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
    public void testNotificationClassParentEntity() {
        List<NotificationClassParent> seedNotificationsClassParent = (List<NotificationClassParent>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "notifications_class_parent.json");

        assertNotNull(seedNotificationsClassParent);
        assertEquals(seedNotificationsClassParent.size(), 1);

        NotificationClassParent seedEntity = seedNotificationsClassParent.get(0);
        // get entity from database
        NotificationClassParent databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(NotificationClassParent.class).get(0);

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
                seedEntity.seedTargetClassName,
                databaseEntity.getTargetClass().getName()
        );
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testNotificationClassTeacherEntity() {
        List<NotificationClassTeacher> seedNotificationsClassTeacher = (List<NotificationClassTeacher>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "notifications_class_teacher.json");

        assertNotNull(seedNotificationsClassTeacher);
        assertEquals(seedNotificationsClassTeacher.size(), 1);

        NotificationClassTeacher seedEntity = seedNotificationsClassTeacher.get(0);
        // get entity from database
        NotificationClassTeacher databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(NotificationClassTeacher.class).get(0);

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
                seedEntity.seedTargetClassName,
                databaseEntity.getTargetClass().getName()
        );
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

    @Test
    @Category(TestCategory.Unit.class)
    public void testNotificationPersonalTeacherEntity() {
        List<NotificationPersonalTeacher> seedNotificationsPersonalTeacher = (List<NotificationPersonalTeacher>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "notifications_personal_teacher.json");

        assertNotNull(seedNotificationsPersonalTeacher);
        assertEquals(seedNotificationsPersonalTeacher.size(), 1);

        NotificationPersonalTeacher seedEntity = seedNotificationsPersonalTeacher.get(0);
        // get entity from database
        NotificationPersonalTeacher databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(NotificationPersonalTeacher.class).get(0);

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
                seedEntity.seedTargetTeacherEmail,
                databaseEntity.getTargetUser().getEmail()
        );
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testNotificationsRead() {
        System.out.println("-------------- TODO testNotificationsRead --------------");
        // TODO
        // before this, it must work:
        // com.github.polimi_mt_acg.utils.json_mappers.SeedEntityNotificationRead$seedAssociateNotificationToUser()
    }
}

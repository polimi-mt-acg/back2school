package com.github.polimi_mt_acg.back2school.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;

import java.lang.Class;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    NotificationClassParent seedEntity = getSeedEntity(NotificationClassParent.class);
    NotificationClassParent databaseEntity = getDatabaseEntity(NotificationClassParent.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);

    // specific asserts
    assertEquals(seedEntity.getSeedTargetClassName(), databaseEntity.getTargetClass().getName());
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationClassTeacherEntity() {
    NotificationClassTeacher seedEntity = getSeedEntity(NotificationClassTeacher.class);
    NotificationClassTeacher databaseEntity = getDatabaseEntity(NotificationClassTeacher.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);

    // specific asserts
    assertEquals(seedEntity.getSeedTargetClassName(), databaseEntity.getTargetClass().getName());
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationGeneralParentsEntity() {
    NotificationGeneralParents seedEntity = getSeedEntity(NotificationGeneralParents.class);
    NotificationGeneralParents databaseEntity = getDatabaseEntity(NotificationGeneralParents.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationGeneralTeachersEntity() {
    NotificationGeneralTeachers seedEntity = getSeedEntity(NotificationGeneralTeachers.class);
    NotificationGeneralTeachers databaseEntity = getDatabaseEntity(NotificationGeneralTeachers.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationPersonalParentEntity() {
    NotificationPersonalParent seedEntity = getSeedEntity(NotificationPersonalParent.class);
    NotificationPersonalParent databaseEntity = getDatabaseEntity(NotificationPersonalParent.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);

    // specific asserts
    assertEquals(seedEntity.getSeedTargetParentEmail(), databaseEntity.getTargetUser().getEmail());
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationPersonalTeacherEntity() {
    NotificationPersonalTeacher seedEntity = getSeedEntity(NotificationPersonalTeacher.class);
    NotificationPersonalTeacher databaseEntity = getDatabaseEntity(NotificationPersonalTeacher.class);

    // common asserts of Notification class
    testNotificationCommonAsserts(seedEntity, databaseEntity);

    // specific asserts
    assertEquals(seedEntity.getSeedTargetTeacherEmail(), databaseEntity.getTargetUser().getEmail());
  }

  @Test
  @Category(TestCategory.Unit.class)
  public void testNotificationsRead() {
    System.out.println("-------------- TODO testNotificationsRead --------------");
    // TODO
    // before this, it must work:
    // com.github.polimi_mt_acg.back2school.utils.json_mappers.SeedEntityNotificationRead$seedAssociateNotificationToUser()
  }

  /**
   * Load a notification entity from the database by its class.
   *
   * @param className Notification class of which retrieve the entity.
   * @param <T> The type of the class of the notification.
   * @return
   */
  private <T> T getDatabaseEntity(Class<T> className) {
    List<T> entities = DatabaseHandler.getInstance().getListSelectFrom(className);
    assertNotNull(entities);
    assertEquals(1, entities.size());
    return entities.get(0);
  }

  /**
   * Retrieve a notification entity from the seeds data by its class.
   *
   * @param className Notification class of which retrieve the entity.
   * @param <T> The type of the class of the notification.
   * @return
   */
  private <T> T getSeedEntity(Class className) {
    List<Notification> seedNotifications =
        (List<Notification>)
            DatabaseSeeder.getEntitiesListFromSeed("scenarioA_unit_tests", "notifications.json");

    List<T> entities =
        (List<T>)
            seedNotifications
                .stream()
                .filter(notification -> notification.getClass().equals(className))
                .collect(Collectors.toList());
    assertNotNull(entities);
    assertEquals(1, entities.size());
    return entities.get(0);
  }

  /**
   * Perform common asserts on a Notification object.
   *
   * @param seedEntity The entity from the seeds
   * @param databaseEntity The entity from the database
   */
  private void testNotificationCommonAsserts(Notification seedEntity, Notification databaseEntity) {
    assertNotNull(seedEntity);

    assertNotNull(databaseEntity);
    assertNotNull(databaseEntity.getCreator());

    assertEquals(seedEntity.getDatetime().toString(), databaseEntity.getDatetime().toString());
    assertEquals(seedEntity.getSubject(), databaseEntity.getSubject());
    assertEquals(seedEntity.getText(), databaseEntity.getText());
  }
}

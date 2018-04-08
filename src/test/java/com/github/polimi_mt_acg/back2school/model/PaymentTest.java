package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.DatabaseSeeder;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PaymentTest {

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
    public void testPaymentEntity() {
        List<Payment> seedPayments = (List<Payment>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "payments.json");

        assertNotNull(seedPayments);
        assertEquals(seedPayments.size(), 1);

        Payment seedEntity = seedPayments.get(0);
        // get payment from database
        Payment databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(Payment.class).get(0);


        // asserts beginning
        assertNotNull(databaseEntity);
        assertEquals(
                seedEntity.seedAssignedToUserEmail,
                databaseEntity.getAssignedTo().getEmail()
        );
        assertEquals(
                seedEntity.seedPlacedByUserEmail,
                databaseEntity.getPlacedBy().getEmail()
        );
        assertEquals(seedEntity.getType(), databaseEntity.getType());
        assertEquals(
                seedEntity.getDatetimeRequested().toString(),
                databaseEntity.getDatetimeRequested().toString()
        );
        assertEquals(
                seedEntity.getDatetimeDone().toString(),
                databaseEntity.getDatetimeDone().toString()
        );
        assertEquals(
                seedEntity.getDatetimeDeadline().toString(),
                databaseEntity.getDatetimeDeadline().toString()
        );
        assertEquals(seedEntity.isDone(), databaseEntity.isDone());
        assertEquals(seedEntity.getSubject(), databaseEntity.getSubject());
        assertEquals(seedEntity.getDescription(), databaseEntity.getDescription());
        assertEquals(seedEntity.getAmount(), databaseEntity.getAmount(), 0.001);
    }
}

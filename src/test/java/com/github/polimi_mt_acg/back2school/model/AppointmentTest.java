package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import org.junit.*;
import org.junit.experimental.categories.Category;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentTest {

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
    public void testAppointmentEntity() {
        List<Appointment> seedAppointments = (List<Appointment>) DatabaseSeeder
                .getEntitiesListFromSeed("scenarioA_unit_tests", "appointments.json");

        assertNotNull(seedAppointments);
        assertEquals(seedAppointments.size(), 1);

        Appointment seedEntity = seedAppointments.get(0);
        // get entity from database
        Appointment databaseEntity = DatabaseHandler
                .getInstance().getListSelectFrom(Appointment.class).get(0);

        // asserts beginning
        assertNotNull(databaseEntity);
        assertNotNull(databaseEntity.getTeacher());
        assertEquals(
                seedEntity.seedTeacherEmail,
                databaseEntity.getTeacher().getEmail()
        );
        assertNotNull(databaseEntity.getParent());
        assertEquals(
                seedEntity.seedParentEmail,
                databaseEntity.getParent().getEmail()
        );
        assertEquals(
                seedEntity.getDatetimeStart().toString(),
                databaseEntity.getDatetimeStart().toString()
        );
        assertEquals(
                seedEntity.getDatetimeEnd().toString(),
                databaseEntity.getDatetimeEnd().toString()
        );
        assertEquals(seedEntity.getStatus(), databaseEntity.getStatus());
    }
}

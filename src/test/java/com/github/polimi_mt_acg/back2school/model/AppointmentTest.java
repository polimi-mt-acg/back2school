package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.TestEntitiesFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AppointmentTest {

    private SessionFactory sessionFactory;
    private User testTeacher;
    private User testParent;
    private Appointment testAppointment;
    //datetimeStart, datetimeEnd, status

    @Before
    public void setUp() throws Exception {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
        // Then create fictitious Entities
        testTeacher = TestEntitiesFactory.buildTeacher();
        testParent = TestEntitiesFactory.buildParent();
        testAppointment = TestEntitiesFactory.buildAppointment();
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testGradeAssociations() {
        // Persist test entities
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(testTeacher);
        session.save(testParent);

        // Link and save testAppointment
        testAppointment.setTeacher(testTeacher);
        testAppointment.setParent(testParent);
        session.save(testAppointment);

        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = sessionFactory.openSession();
        session.beginTransaction();
        Appointment ap = session.get(Appointment.class, testAppointment.getId());


        session.getTransaction().commit();
        session.close();

        assertNotNull(ap);

        List<Appointment> appointments = new ArrayList<>();
        appointments.add(ap);


        for (Appointment a: appointments) {
            // Teacher and parent data test
            assertEquals(a.getTeacher().getId(), testTeacher.getId());
            assertEquals(a.getTeacher().getName(), testTeacher.getName());
            assertEquals(a.getTeacher().getEmail(), testTeacher.getEmail());
            assertEquals(a.getTeacher().getPassword(), testTeacher.getPassword());
            assertEquals(a.getTeacher().getSalt(), testTeacher.getSalt());
            assertEquals(a.getTeacher().getSurname(), testTeacher.getSurname());
            assertEquals(a.getTeacher().getRole(), testTeacher.getRole());
            assertEquals(a.getTeacher().getClass(), testTeacher.getClass());

            assertEquals(a.getParent().getId(), testParent.getId());
            assertEquals(a.getParent().getName(), testParent.getName());
            assertEquals(a.getParent().getSurname(), testParent.getSurname());
            assertEquals(a.getParent().getPassword(), testParent.getPassword());
            assertEquals(a.getParent().getSalt(), testParent.getSalt());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }

        // Appointment data
        assertEquals(ap.getDatetimeStart(), testAppointment.getDatetimeStart());
        assertEquals(ap.getDatetimeEnd(), testAppointment.getDatetimeEnd());
        assertEquals(ap.getStatus(), testAppointment.getStatus());
    }


}

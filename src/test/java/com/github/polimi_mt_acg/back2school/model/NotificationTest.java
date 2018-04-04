package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.utils.TestEntitiesFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationTest {// Test members
    private SessionFactory sessionFactory;
    private User testAdministrator;
    private User testTeacher;
    private User testParent;
    private User testStudent1;
    private User testStudent2;
    private SchoolClass testClass;
    private NotificationPersonalParent testNotificationPersonalParent;
    private NotificationPersonalTeacher testNotificationPersonalTeacher;
    private NotificationClassParent testNotificationClassParent;
    private NotificationClassTeacher testNotificationClassTeacher;
    private NotificationGeneral testNotificationGeneral;
    private List<User> ls = new ArrayList<>();


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
        testAdministrator = TestEntitiesFactory.buildAdministrator();
        testParent = TestEntitiesFactory.buildParent();
        testTeacher = TestEntitiesFactory.buildTeacher();
        testClass = TestEntitiesFactory.buildSchoolClass();
        testStudent1 = TestEntitiesFactory.buildStudent();
        testStudent2 = TestEntitiesFactory.buildStudent();
        testNotificationPersonalParent = (NotificationPersonalParent) TestEntitiesFactory.buildNotification(NotificationPersonalParent.class);
        testNotificationPersonalTeacher = (NotificationPersonalTeacher) TestEntitiesFactory.buildNotification(NotificationPersonalTeacher.class);
        testNotificationClassParent = (NotificationClassParent) TestEntitiesFactory.buildNotification(NotificationClassParent.class);
        testNotificationClassTeacher = (NotificationClassTeacher) TestEntitiesFactory.buildNotification(NotificationClassTeacher.class);
        testNotificationGeneral = (NotificationGeneral) TestEntitiesFactory.buildNotification(NotificationGeneral.class);
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testNotificationAssociations() {
        // Persist test entities
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(testAdministrator);
        session.save(testParent);
        session.save(testTeacher);

        //Link and save testClass/

        testClass.setStudentsOfTheClass(ls);
        session.save(testClass);

        // Link and save testNotificationPersonalParent
        testNotificationPersonalParent.setCreator(testAdministrator);
        testNotificationPersonalParent.setTarget(testParent);
        session.save(testNotificationPersonalParent);

        // Link and save testNotificationPersonalTeacher
        testNotificationPersonalTeacher.setCreator(testAdministrator);
        testNotificationPersonalTeacher.setTarget(testTeacher);
        session.save(testNotificationPersonalTeacher);

        // Link and save testNotificationClassParent
        testNotificationClassParent.setCreator(testAdministrator);
        testNotificationClassParent.setTarget(testClass);
        session.save(testNotificationClassParent);

        // Link and save testNotificationClassTeacher
        testNotificationClassTeacher.setCreator(testAdministrator);
        testNotificationClassTeacher.setTarget(testClass);
        session.save(testNotificationClassTeacher);

        // Link and save testNotificationGeneral
        testNotificationGeneral.setCreator(testAdministrator);
        session.save(testNotificationGeneral);


        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = sessionFactory.openSession();
        session.beginTransaction();
        NotificationPersonalParent nPP = session.get(NotificationPersonalParent.class, testNotificationPersonalParent.getId());
        NotificationPersonalTeacher nPT = session.get(NotificationPersonalTeacher.class, testNotificationPersonalTeacher.getId());
        NotificationClassParent nCP = session.get(NotificationClassParent.class, testNotificationClassParent.getId());
        NotificationClassTeacher nCT = session.get(NotificationClassTeacher.class, testNotificationClassTeacher.getId());
        NotificationGeneral nG = session.get(NotificationGeneral.class, testNotificationGeneral.getId());

        session.getTransaction().commit();
        session.close();

        assertNotNull(nPP);
        assertNotNull(nPT);
        assertNotNull(nCP);
        assertNotNull(nCT);
        assertNotNull(nG);

        List<NotificationPersonalParent> notificationPP = new ArrayList<>();
        notificationPP.add(nPP);
        List<NotificationPersonalTeacher> notificationPT = new ArrayList<>();
        notificationPT.add(nPT);
        List<NotificationClassParent> notificationCP = new ArrayList<>();
        notificationCP.add(nCP);
        List<NotificationClassTeacher> notificationCT = new ArrayList<>();
        notificationCT.add(nCT);
        List<NotificationGeneral> notificationG = new ArrayList<>();
        notificationG.add(nG);

        // Notification Personal Parent data
        for (NotificationPersonalParent p: notificationPP) {
            // Administrator and parent data test
            assertEquals(p.getCreator().getId(), testAdministrator.getId());
            assertEquals(p.getCreator().getName(), testAdministrator.getName());
            assertEquals(p.getCreator().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getCreator().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getCreator().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());

            assertEquals(p.getTarget().getId(), testParent.getId());
            assertEquals(p.getTarget().getName(), testParent.getName());
            assertEquals(p.getTarget().getSurname(), testParent.getSurname());
            assertEquals(p.getTarget().getPassword(), testParent.getPassword());
            assertEquals(p.getTarget().getSalt(), testParent.getSalt());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }


        assertEquals(nPP.getDatetime(), testNotificationPersonalParent.getDatetime());
        assertEquals(nPP.getSubject(), testNotificationPersonalParent.getSubject());
        assertEquals(nPP.getText(), testNotificationPersonalParent.getText());

// Notification Personal Teacher data
        for (NotificationPersonalTeacher p: notificationPT) {
            // Administrator and parent data test
            assertEquals(p.getCreator().getId(), testAdministrator.getId());
            assertEquals(p.getCreator().getName(), testAdministrator.getName());
            assertEquals(p.getCreator().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getCreator().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getCreator().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());

            assertEquals(p.getTarget().getId(), testTeacher.getId());
            assertEquals(p.getTarget().getName(), testTeacher.getName());
            assertEquals(p.getTarget().getSurname(), testTeacher.getSurname());
            assertEquals(p.getTarget().getPassword(), testTeacher.getPassword());
            assertEquals(p.getTarget().getSalt(), testTeacher.getSalt());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }

        assertEquals(nPT.getDatetime(), testNotificationPersonalTeacher.getDatetime());
        assertEquals(nPT.getSubject(), testNotificationPersonalTeacher.getSubject());
        assertEquals(nPT.getText(), testNotificationPersonalTeacher.getText());

// Notification Class Parent data
        for (NotificationClassParent p: notificationCP) {
            // Administrator and parent data test
            assertEquals(p.getCreator().getId(), testAdministrator.getId());
            assertEquals(p.getCreator().getName(), testAdministrator.getName());
            assertEquals(p.getCreator().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getCreator().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getCreator().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());

            assertEquals(p.getTarget().getId(), testClass.getId());
            assertEquals(p.getTarget().getName(), testClass.getName());
            assertEquals(p.getTarget().getAcademicYear(), testClass.getAcademicYear());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }

        assertEquals(nCP.getDatetime(), testNotificationClassParent.getDatetime());
        assertEquals(nCP.getSubject(), testNotificationClassParent.getSubject());
        assertEquals(nCP.getText(), testNotificationClassParent.getText());


// Notification Class Teacher data
        for (NotificationClassTeacher p: notificationCT) {
            // Administrator and parent data test
            assertEquals(p.getCreator().getId(), testAdministrator.getId());
            assertEquals(p.getCreator().getName(), testAdministrator.getName());
            assertEquals(p.getCreator().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getCreator().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getCreator().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());

            assertEquals(p.getTarget().getId(), testClass.getId());
            assertEquals(p.getTarget().getName(), testClass.getName());
            assertEquals(p.getTarget().getAcademicYear(), testClass.getAcademicYear());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }

        assertEquals(nCT.getDatetime(), testNotificationClassTeacher.getDatetime());
        assertEquals(nCT.getSubject(), testNotificationClassTeacher.getSubject());
        assertEquals(nCT.getText(), testNotificationClassTeacher.getText());

// Notification General data
        for (NotificationGeneral p: notificationG) {
            // Administrator and parent data test
            assertEquals(p.getCreator().getId(), testAdministrator.getId());
            assertEquals(p.getCreator().getName(), testAdministrator.getName());
            assertEquals(p.getCreator().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getCreator().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getCreator().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());
        }

        assertEquals(nG.getDatetime(), testNotificationGeneral.getDatetime());
        assertEquals(nG.getSubject(), testNotificationGeneral.getSubject());
        assertEquals(nG.getText(), testNotificationGeneral.getText());


    }
}

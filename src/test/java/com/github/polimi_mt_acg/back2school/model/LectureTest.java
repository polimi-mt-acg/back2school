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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LectureTest {
    // Test members
    private SessionFactory sessionFactory;
    private Lecture testLecture;
    private User testTeacher;
    private Subject testSubject;
    private Classroom testClassroom;

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
        testSubject = TestEntitiesFactory.buildSubject();
        testClassroom = TestEntitiesFactory.buildClassroom();
        testLecture = TestEntitiesFactory.buildLecture();
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testLectureAssociations() {
        // Persist test entities
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(testTeacher);
        session.save(testClassroom);
        session.save(testSubject);

        // Link testLecture to other entities
        testLecture.setTeacher(testTeacher);
        testLecture.setClassroom(testClassroom);
        testLecture.setSubject(testSubject);
        session.save(testLecture);

        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = sessionFactory.openSession();
        session.beginTransaction();
        Lecture l = session.get(Lecture.class, testLecture.getId());

        session.getTransaction().commit();
        session.close();

        assertNotNull(l);

        // Teacher data
        assertEquals(l.getTeacher().getId(), testTeacher.getId());
        assertEquals(l.getTeacher().getName(), testTeacher.getName());
        assertEquals(l.getTeacher().getSurname(), testTeacher.getSurname());
        assertEquals(l.getTeacher().getPassword(), testTeacher.getPassword());
        assertEquals(l.getTeacher().getSalt(), testTeacher.getSalt());
//        assertEquals(l.getTeacher().getType(), testTeacher.getType());

        // Subject data
        assertEquals(l.getSubject().getId(), testSubject.getId());
        assertEquals(l.getSubject().getName(), testSubject.getName());
        assertEquals(l.getSubject().getDescription(), testSubject.getDescription());

        // Classroom data
        assertEquals(l.getClassroom().getId(), testClassroom.getId());
        assertEquals(l.getClassroom().getName(), testClassroom.getName());
        assertEquals(l.getClassroom().getFloor(), testClassroom.getFloor());
        assertEquals(l.getClassroom().getBuilding(), testClassroom.getBuilding());
    }
}

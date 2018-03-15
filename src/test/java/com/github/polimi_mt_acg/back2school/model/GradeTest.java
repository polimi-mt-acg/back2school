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

public class GradeTest {
    // Test members
    private SessionFactory sessionFactory;
    private Grade testGrade;
    private User testStudent;
    private User testTeacher;
    private Subject testSubject;

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
        testGrade = TestEntitiesFactory.buildGrade();
        testStudent = TestEntitiesFactory.buildStudent();
        testTeacher = TestEntitiesFactory.buildTeacher();
        testSubject = TestEntitiesFactory.buildSubject();
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testGradeAssociations() {
        // Persist test entities
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(testStudent);
        session.save(testTeacher);
        session.save(testSubject);

        // Link testStudent and testSubject to testGrade
        testGrade.setStudent(testStudent);
        testGrade.setTeacher(testTeacher);
        testGrade.setSubject(testSubject);
        session.save(testGrade);

        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = sessionFactory.openSession();
        session.beginTransaction();
        Grade g = session.get(Grade.class, testGrade.getId());

        session.getTransaction().commit();
        session.close();

        assertNotNull(g);

        // Student data
        assertEquals(g.getStudent().getId(), testStudent.getId());
        assertEquals(g.getStudent().getName(), testStudent.getName());
        assertEquals(g.getStudent().getSurname(), testStudent.getSurname());
        assertEquals(g.getStudent().getPassword(), testStudent.getPassword());
        assertEquals(g.getStudent().getSalt(), testStudent.getSalt());
        assertEquals(g.getStudent().getType(), testStudent.getType());

        // Teacher data
        assertEquals(g.getTeacher().getId(), testTeacher.getId());
        assertEquals(g.getTeacher().getName(), testTeacher.getName());
        assertEquals(g.getTeacher().getSurname(), testTeacher.getSurname());
        assertEquals(g.getTeacher().getPassword(), testTeacher.getPassword());
        assertEquals(g.getTeacher().getSalt(), testTeacher.getSalt());
        assertEquals(g.getTeacher().getType(), testTeacher.getType());

        // Subject data
        assertEquals(g.getSubject().getId(), testSubject.getId());
        assertEquals(g.getSubject().getName(), testSubject.getName());
        assertEquals(g.getSubject().getDescription(), testSubject.getDescription());

    }
}
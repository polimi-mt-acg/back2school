package com.github.polimi_mt_acg.back2school.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GradeTest {
    // testStudent data
    private final String studentName = "testStudentName";
    private final String studentSurname = "testStudentSurname";
    private final String studentEmail = "testStudent@email.com";
    private final String studentPassword = "testStudentPassword";
    private final String studentSalt = "testStudentSalt";
    private final User.Type studentType = User.Type.STUDENT;
    // testTeacher data
    private final String teacherName = "testTeacherName";
    private final String teacherSurname = "testTeacherSurname";
    private final String teacherEmail = "testTeacher@email.com";
    private final String teacherPassword = "testTeacherPassword";
    private final String teacherSalt = "testTeacherSalt";
    private final User.Type teacherType = User.Type.TEACHER;
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
        buildTestGrade();
        buildTestStudent();
        buildTestTeacher();
        buildTestSubject();
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

    private void buildTestGrade() {
        testGrade = new Grade();
        testGrade.setDate(LocalDate.now());
        testGrade.setGrade(7.5);
        testGrade.setTitle("Midterm Latin exam");
    }

    private void buildTestStudent() {
        testStudent = new User();
        testStudent.setType(studentType);
        testStudent.setName(studentName);
        testStudent.setSurname(studentSurname);
        testStudent.setEmail(studentEmail);
        testStudent.setPassword(studentPassword);
        testStudent.setSalt(studentSalt);
    }

    private void buildTestTeacher() {
        testTeacher = new User();
        testTeacher.setType(teacherType);
        testTeacher.setName(teacherName);
        testTeacher.setSurname(teacherSurname);
        testTeacher.setEmail(teacherEmail);
        testTeacher.setPassword(teacherPassword);
        testTeacher.setSalt(teacherSalt);
    }

    private void buildTestSubject() {
        testSubject = new Subject();
        testSubject.setName("Latin");
        testSubject.setDescription("Rosa, rosae, rosae, rosam, rosa, rosa");
    }
}
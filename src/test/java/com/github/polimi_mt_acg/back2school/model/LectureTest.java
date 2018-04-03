package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.utils.TestEntitiesFactory;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LectureTest {
    // Test members
    private Session session;
    private Lecture testLecture;
    private User testTeacher;
    private Subject testSubject;
    private Classroom testClassroom;

    @Before
    public void setUp() throws Exception {
        // Then create fictitious Entities
        testTeacher = TestEntitiesFactory.buildTeacher();
        testSubject = TestEntitiesFactory.buildSubject();
        testClassroom = TestEntitiesFactory.buildClassroom();
        testLecture = TestEntitiesFactory.buildLecture();
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHandler.getInstance().truncateDatabase();

        if (session != null) {
            session.close();
        }
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testLectureAssociations() {
        session = DatabaseHandler.getInstance().getNewSession();

        // Persist test entities
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
        session = DatabaseHandler.getInstance().getNewSession();
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

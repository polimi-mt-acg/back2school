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


public class AuthenticationSessionTest {

    // Test members
    private Session session;
    private User testAdministrator;
    private User testTeacher;
    private User testParent;
    private AuthenticationSession testSessionAdministrator;
    private AuthenticationSession testSessionParent;
    private AuthenticationSession testSessionTeacher;
    // the student cannot login into the system

    @Before
    public void setUp() {
        // Then create fictitious Entities
        testAdministrator = TestEntitiesFactory.buildAdministrator();
        testTeacher = TestEntitiesFactory.buildTeacher();
        testParent = TestEntitiesFactory.buildParent();
        testSessionAdministrator = TestEntitiesFactory.buildAuthenticationSession();
        testSessionTeacher = TestEntitiesFactory.buildAuthenticationSession();
        testSessionParent = TestEntitiesFactory.buildAuthenticationSession();
    }

    @After
    public void tearDown() {
        DatabaseHandler.getInstance().truncateDatabase();

        if (session != null) {
            session.close();
        }
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testAuthenticationSessionAssociations() {
        session = DatabaseHandler.getInstance().getNewSession();

        // Persist test entities
        session.beginTransaction();

        session.save(testAdministrator);
        session.save(testTeacher);
        session.save(testParent);

        testSessionAdministrator.setUser(testAdministrator);
        testSessionTeacher.setUser(testTeacher);
        testSessionParent.setUser(testParent);

        session.save(testSessionAdministrator);
        session.save(testSessionTeacher);
        session.save(testSessionParent);

        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = DatabaseHandler.getInstance().getNewSession();
        session.beginTransaction();

        AuthenticationSession ASA = session.get(AuthenticationSession.class, testSessionAdministrator.getId());
        AuthenticationSession ASP = session.get(AuthenticationSession.class, testSessionParent.getId());
        AuthenticationSession AST = session.get(AuthenticationSession.class, testSessionTeacher.getId());

        session.getTransaction().commit();
        session.close();

        assertNotNull(ASA);
        assertNotNull(ASP);
        assertNotNull(AST);

        assertEquals(ASA.getUser().getId(), testAdministrator.getId());
        assertEquals(AST.getUser().getId(), testTeacher.getId());
        assertEquals(ASP.getUser().getId(), testParent.getId());
    }
}

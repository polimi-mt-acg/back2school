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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class AuthenticationSessionTest {

    // Test members
    private SessionFactory sessionFactory;
    private User testAdministrator;
    private User testTeacher;
    private User testParent;
    private AuthenticationSession testSessionAdministrator;
    private AuthenticationSession testSessionParent;
    private AuthenticationSession testSessionTeacher;
    // the student cannot login into the system

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
        testTeacher = TestEntitiesFactory.buildTeacher();
        testParent = TestEntitiesFactory.buildParent();
        testSessionAdministrator = TestEntitiesFactory.buildAuthenticationSession();
        testSessionTeacher = TestEntitiesFactory.buildAuthenticationSession();
        testSessionParent = TestEntitiesFactory.buildAuthenticationSession();
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    @Category(TestCategory.Unit.class)
    public void testAuthenticationSessionAssociations() {
        // Persist test entities
        Session sessionFactory = this.sessionFactory.openSession();
        sessionFactory.beginTransaction();

        sessionFactory.save(testAdministrator);
        sessionFactory.save(testTeacher);
        sessionFactory.save(testParent);

        testSessionAdministrator.setUser(testAdministrator);
        testSessionTeacher.setUser(testTeacher);
        testSessionParent.setUser(testParent);

        sessionFactory.save(testSessionAdministrator);
        sessionFactory.save(testSessionTeacher);
        sessionFactory.save(testSessionParent);

        sessionFactory.getTransaction().commit();
        sessionFactory.close();

        // Now we check how Hibernate fetches foreign keys' data
        sessionFactory = this.sessionFactory.openSession();
        sessionFactory.beginTransaction();

        AuthenticationSession ASA = sessionFactory.get(AuthenticationSession.class, testSessionAdministrator.getId());
        AuthenticationSession ASP = sessionFactory.get(AuthenticationSession.class, testSessionParent.getId());
        AuthenticationSession AST = sessionFactory.get(AuthenticationSession.class, testSessionTeacher.getId());

        sessionFactory.getTransaction().commit();
        sessionFactory.close();

        assertNotNull(ASA);
        assertNotNull(ASP);
        assertNotNull(AST);

        assertEquals(ASA.getUser().getId(), testAdministrator.getId());
        assertEquals(AST.getUser().getId(), testTeacher.getId());
        assertEquals(ASP.getUser().getId(), testParent.getId());
    }
}

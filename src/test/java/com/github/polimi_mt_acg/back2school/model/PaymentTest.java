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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PaymentTest {// Test members
    private SessionFactory sessionFactory;
    private User testAdministrator;
    private User testParent;
    private Payment testPaymentMaterial;
    private Payment testPaymentMonthly;
    private Payment testPaymentTrip;

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
        testPaymentMaterial = TestEntitiesFactory.buildPayment(Payment.Type.MATERIAL);
        testPaymentMonthly = TestEntitiesFactory.buildPayment(Payment.Type.MONTHLY);
        testPaymentTrip = TestEntitiesFactory.buildPayment(Payment.Type.TRIP);
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

        session.save(testAdministrator);
        session.save(testParent);

        // Link and save testPaymentMaterial
        testPaymentMaterial.setPlacedBy(testAdministrator);
        testPaymentMaterial.setAssignedTo(testParent);
        session.save(testPaymentMaterial);

        // Link and save testPaymentTrip
        testPaymentTrip.setPlacedBy(testAdministrator);
        testPaymentTrip.setAssignedTo(testParent);
        session.save(testPaymentTrip);

        // Link and save testPaymentMonthly
        testPaymentMonthly.setPlacedBy(testAdministrator);
        testPaymentMonthly.setAssignedTo(testParent);
        session.save(testPaymentMonthly);

        session.getTransaction().commit();
        session.close();

        // Now we check how Hibernate fetches foreign keys' data
        session = sessionFactory.openSession();
        session.beginTransaction();
        Payment pMa = session.get(Payment.class, testPaymentMaterial.getId());
        Payment pMo = session.get(Payment.class, testPaymentMonthly.getId());
        Payment pT = session.get(Payment.class, testPaymentTrip.getId());

        session.getTransaction().commit();
        session.close();

        assertNotNull(pMa);
        assertNotNull(pMo);
        assertNotNull(pT);

        List<Payment> payments = new ArrayList<>();
        payments.add(pMa);
        payments.add(pMo);
        payments.add(pT);

        for (Payment p: payments) {
            // Administrator and parent data test
            assertEquals(p.getPlacedBy().getId(), testAdministrator.getId());
            assertEquals(p.getPlacedBy().getName(), testAdministrator.getName());
            assertEquals(p.getPlacedBy().getSurname(), testAdministrator.getSurname());
            assertEquals(p.getPlacedBy().getPassword(), testAdministrator.getPassword());
            assertEquals(p.getPlacedBy().getSalt(), testAdministrator.getSalt());
//            assertEquals(p.getPlacedBy().getType(), testAdministrator.getType());

            assertEquals(p.getAssignedTo().getId(), testParent.getId());
            assertEquals(p.getAssignedTo().getName(), testParent.getName());
            assertEquals(p.getAssignedTo().getSurname(), testParent.getSurname());
            assertEquals(p.getAssignedTo().getPassword(), testParent.getPassword());
            assertEquals(p.getAssignedTo().getSalt(), testParent.getSalt());
//            assertEquals(p.getAssignedTo().getType(), testParent.getType());
        }

        // Payment Material data
        assertEquals(pMa.getDatetimeRequested(), testPaymentMaterial.getDatetimeRequested());
        assertEquals(pMa.getDatetimeDone(), testPaymentMaterial.getDatetimeDone());
        assertEquals(pMa.getDatetimeDeadline(), testPaymentMaterial.getDatetimeDeadline());
        assertEquals(pMa.isDone(), testPaymentMaterial.isDone());
        assertEquals(pMa.getSubject(), testPaymentMaterial.getSubject());
        assertEquals(pMa.getDescription(), testPaymentMaterial.getDescription());
        assertEquals(pMa.getAmount(), testPaymentMaterial.getAmount(), 0.0001);

        // Payment Monthly data
        assertEquals(pMo.getDatetimeRequested(), testPaymentMonthly.getDatetimeRequested());
        assertEquals(pMo.getDatetimeDone(), testPaymentMonthly.getDatetimeDone());
        assertEquals(pMo.getDatetimeDeadline(), testPaymentMonthly.getDatetimeDeadline());
        assertEquals(pMo.isDone(), testPaymentMonthly.isDone());
        assertEquals(pMo.getSubject(), testPaymentMonthly.getSubject());
        assertEquals(pMo.getDescription(), testPaymentMonthly.getDescription());
        assertEquals(pMo.getAmount(), testPaymentMonthly.getAmount(), 0.0001);

        // Payment material data
        assertEquals(pT.getDatetimeRequested(), testPaymentTrip.getDatetimeRequested());
        assertEquals(pT.getDatetimeDone(), testPaymentTrip.getDatetimeDone());
        assertEquals(pT.getDatetimeDeadline(), testPaymentTrip.getDatetimeDeadline());
        assertEquals(pT.isDone(), testPaymentTrip.isDone());
        assertEquals(pT.getSubject(), testPaymentTrip.getSubject());
        assertEquals(pT.getDescription(), testPaymentTrip.getDescription());
        assertEquals(pT.getAmount(), testPaymentTrip.getAmount(), 0.0001);
    }
}

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
    private Class testClass;
    private NotificationPersonalParent testNotificationPersonalParent;
    private NotificationPersonalTeacher testNotificationPersonalTeacher;
    private NotificationClassParent testNotificationClassParent;
    private NotificationClassTeacher testNotificationClassTeacher;
    private NotificationGeneral testNotificationGeneral;


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
        testClass = TestEntitiesFactory.buildClass();
        testNotificationPersonalParent = (NotificationPersonalParent) TestEntitiesFactory.buildNotification(NotificationPersonalParent.class);
        testNotificationPersonalTeacher = (NotificationPersonalTeacher) TestEntitiesFactory.buildNotification(NotificationPersonalTeacher.class);
        testNotificationClassParent = (NotificationClassParent) TestEntitiesFactory.buildNotification(NotificationClassParent.class);
        testNotificationClassTeacher = (NotificationClassTeacher) TestEntitiesFactory.buildNotification(NotificationClassTeacher.class);
        testNotificationGeneral = (NotificationGeneral) TestEntitiesFactory.buildNotification(NotificationGeneral.class);
    }


}
package com.github.polimi_mt_acg.back2school.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Table;

import java.util.logging.Logger;

public class DatabaseHandler {
    private static final Logger LOGGER;
    private static volatile DatabaseHandler instance;

    private static final StandardServiceRegistry registry;
    private static final SessionFactory sessionFactory;

    static {
        LOGGER = Logger.getLogger(DatabaseHandler.class.getName());

        // Initialize once the register
        registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();

        // Initialize once the session factory
        try {
            sessionFactory = new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();
        }
        catch(Exception e) {
            // The registry would be destroyed by the SessionFactory,
            // but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    private DatabaseHandler() { }

    public static DatabaseHandler getInstance() {
        if (instance == null) {
            synchronized(DatabaseHandler.class) {
                if (instance == null) {
                    instance = new DatabaseHandler();
                }
            }
        }
        return instance;
    }

    public Session getNewSession() {
        return sessionFactory.openSession();
    }

    public void truncateDatabase() {
        Session session = getInstance().getNewSession();
        session.beginTransaction();

        LOGGER.info("Executing truncateDatabase");
        Metadata metadata = new MetadataSources(registry).buildMetadata();

        // shut off the foreign key checks
        session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();

        // Iterate over namespaces
        for (Namespace namespace : metadata.getDatabase().getNamespaces()) {
            if (namespace == null) {
                LOGGER.warning("Found null namespace");
                break;
            }

            for (Table table : namespace.getTables()) {
                if (table == null) {
                    LOGGER.warning("Found null table");
                    break;
                }
                String tableName = table.getName();

                if (!tableName.equals("hibernate_sequence")){
                    String query =
                            String.format("TRUNCATE TABLE %s", table.getName());

                    LOGGER.info("Executing query: " + query);
                    session.createNativeQuery(query).executeUpdate();
                }
            }
        }

        // turn on again the foreign key checks
        session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        session.getTransaction().commit();
        session.close();
        LOGGER.info("Database truncated");
    }
}

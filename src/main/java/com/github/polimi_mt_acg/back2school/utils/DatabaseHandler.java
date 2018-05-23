package com.github.polimi_mt_acg.back2school.utils;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.relational.Namespace;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.Table;

public class DatabaseHandler {

  private static final Logger LOGGER;
  private static final StandardServiceRegistry registry;
  private static final SessionFactory sessionFactory;
  private static volatile DatabaseHandler instance;

  static {
    LOGGER = Logger.getLogger(DatabaseHandler.class.getName());

    // Initialize once the register
    registry =
        new StandardServiceRegistryBuilder()
            .configure() // configures settings from hibernate.cfg.xml
            .build();

    // Initialize once the session factory
    try {
      sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    } catch (Exception e) {
      // The registry would be destroyed by the SessionFactory,
      // but we had trouble building the SessionFactory
      // so destroy it manually.
      StandardServiceRegistryBuilder.destroy(registry);
      throw e;
    }
  }

  private DatabaseHandler() {}

  public static DatabaseHandler getInstance() {
    if (instance == null) {
      synchronized (DatabaseHandler.class) {
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

  public <T> CriteriaQuery<T> getCriteriaQuery(Class<T> classType) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();

    CriteriaQuery<T> criteria = builder.createQuery(classType);
    return criteria;
  }

  /**
   * List of entities for the SQL query: SELECT * FROM <classType entity>
   *
   * @param classType entity class
   * @param <T> entity type
   */
  public <T> List<T> getListSelectFrom(Class<T> classType) {
    Session session = getNewSession();

    CriteriaQuery<T> criteria = getCriteriaQuery(classType);
    Root<T> root = criteria.from(classType);
    criteria.select(root);

    List<T> entities = session.createQuery(criteria).getResultList();
    session.close();
    return entities;
  }

  /**
   * List of entities for the SQL query: SELECT * FROM <classType entity> WHERE <singularAttribute>
   * = <obj>
   *
   * @param classType entity class
   * @param singularAttribute entity attribute on which perform the query
   * @param obj the value to look for
   * @param <T> entity type
   */
  public <T> List<T> getListSelectFromWhereEqual(
      Class<T> classType, SingularAttribute singularAttribute, Object obj) {
    Session session = getNewSession();
    CriteriaBuilder builder = session.getCriteriaBuilder();

    CriteriaQuery<T> criteria = builder.createQuery(classType);
    Root<T> root = criteria.from(classType);
    criteria.select(root);
    criteria.where(builder.equal(root.get(singularAttribute), obj));

    List<T> results = session.createQuery(criteria).getResultList();
    session.close();
    return results;
  }

  /**
   * List of entities for the SQL query: SELECT * FROM <classType entity> WHERE <singularAttribute>
   * = <obj>
   *
   * <p>This overload takes a session as an additional parameter. The session must be managed by the
   * caller, so transactions and connection status are not handled.
   *
   * @param classType entity class
   * @param singularAttribute entity attribute on which perform the query
   * @param obj the value to look for
   * @param session the Hibernate database session
   * @param <T> entity type
   */
  public <T> List<T> getListSelectFromWhereEqual(
      Class<T> classType, SingularAttribute singularAttribute, Object obj, Session session) {
    CriteriaBuilder builder = session.getCriteriaBuilder();

    CriteriaQuery<T> criteria = builder.createQuery(classType);
    Root<T> root = criteria.from(classType);
    criteria.select(root);
    criteria.where(builder.equal(root.get(singularAttribute), obj));

    List<T> results = session.createQuery(criteria).getResultList();
    return results;
  }

  public void truncateDatabase() {
    Session session = getInstance().getNewSession();
    session.beginTransaction();

    //        LOGGER.info("Executing truncateDatabase");
    Metadata metadata = new MetadataSources(registry).buildMetadata();

    // shutoff the foreign key checks
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

        if (!tableName.equals("hibernate_sequence")) {
          // String query = String.format("TRUNCATE TABLE `%s`", tableName);
          String delete = String.format("DELETE FROM `%s`;", tableName);
          session.createNativeQuery(delete).executeUpdate();
        }
      }
    }

    // turn on again the foreign key checks
    session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

    // reset hibernate auto increment reference indexes
    session.createNativeQuery("UPDATE hibernate_sequence SET next_val = 1").executeUpdate();

    session.getTransaction().commit();
    session.close();
    //        LOGGER.info("Database truncated");
  }

  /**
   * Fetch a single entity from one of its attributes.
   *
   * @param classType The entity class to fetch.
   * @param singularAttribute The attribute of the class on which to perform the query.
   * @param parameter The value of the attribute to look for.
   * @param <T> Type of the entity class.
   * @return Optional entity.
   */
  public static <T> T fetchEntityBy(
      Class<T> classType, SingularAttribute singularAttribute, Object parameter) {

    Session session = DatabaseHandler.getInstance().getNewSession();
    T entity = fetchEntityBy(classType, singularAttribute, parameter, session);
    session.close();
    return entity;
  }

  /**
   * Fetch a single entity from one of its attributes.
   *
   * @param classType The entity class to fetch.
   * @param singularAttribute The attribute of the class on which to perform the query.
   * @param parameter The value of the attribute to look for.
   * @param session The hibernate session.
   * @param <T> Type of the entity class.
   * @return Optional entity.
   */
  public static <T> T fetchEntityBy(
      Class<T> classType, SingularAttribute singularAttribute, Object parameter, Session session) {
    List<T> res =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(classType, singularAttribute, parameter, session);

    if (res.isEmpty()) {
      return null;
    }
    return res.get(0);
  }
}

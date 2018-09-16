package com.github.polimi_mt_acg.back2school.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.model.DeserializeToPersistInterface;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.json_mappers.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.hibernate.Session;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;


public class DatabaseSeeder {

  private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());

  private static final Map<String, Object> seedsMap;

  static {
    Map<String, Object> map = new LinkedHashMap<>();

    /* Seeds to be deployed
     * [x] subject
     * [x] classroom
     * [x] lecture
     * [x] school_class
     * [x] grade
     * [x] user
     * [x] appointment
     * [x] notification_general
     * [x] notification_class_parent
     * [x] notification_class_teacher
     * [x] notification_personal_parent
     * [x] notification_personal_teacher
     * [x] notification_read
     * [x] parent_children
     * [x] payment
     * [x] authentication_session
     */
    // zero dependencies from other entities
    map.put("classrooms.json", ClassroomsJSONTemplate.class);
    map.put("subjects.json", SubjectsJSONTemplate.class);
    map.put("users.json", UsersJSONTemplate.class);

    // one or more dependency from other entities
    map.put("parent_children.json", ParentChildrenJSONTemplate.class);
    map.put("payments.json", PaymentsJSONTemplate.class);
    map.put("grades.json", GradesJSONTemplate.class);
    map.put("classes.json", ClassesJSONTemplate.class);
    map.put("lectures.json", LecturesJSONTemplate.class);
    map.put("appointments.json", AppointmentsJSONTemplate.class);
    map.put("notifications.json", NotificationsJSONTemplate.class);
    map.put("notifications_read.json", NotificationsReadJSONTemplate.class);
    map.put("authentication_sessions.json", AuthenticationSessionJSONTemplate.class);

    seedsMap = Collections.unmodifiableMap(map);
  }

  /**
   * Deploy new seeds to the database by specifying the folder name in order to load a specific
   * scenario.
   *
   * <p>E.g. DatabaseSeeder.deployScenario("scenarioA") It will look into the folder
   * src/test/resources/scenarios_seeds/scenarioA/ for Json files from which to load the data.
   *
   * @param scenarioFolderName
   */
  public static void deployScenario(String scenarioFolderName) {
    for (Map.Entry<String, Object> sm : seedsMap.entrySet()) {
      String seedFilename = sm.getKey();

      List<?> entitiesToPersist = getEntitiesListFromSeed(scenarioFolderName, seedFilename);

      if (entitiesToPersist != null) {
        Session session = DatabaseHandler.getInstance().getNewSession();
        session.beginTransaction();
        for (Object genericEntity : entitiesToPersist) {
          if (genericEntity instanceof DeserializeToPersistInterface) {

            // cast the entity
            DeserializeToPersistInterface entity = (DeserializeToPersistInterface) genericEntity;

            // notify the entity that it will be persisted
            entity.prepareToPersist();

            session.persist(entity);
          }
        }
        session.getTransaction().commit();
        session.close();
//        LOGGER.info(
//            String.format(
//                "deployed seed file: %s/%s",
//                scenarioFolderName,
//                seedFilename
//            )
//        );
      }
    }
  }


  public static List<?> getEntitiesListFromSeed(String scenarioFolderName, String seedFilename) {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);

    StringBuilder seedFilePath = new StringBuilder();
    seedFilePath.append("scenarios_seeds");
    seedFilePath.append(File.separator);
    seedFilePath.append(scenarioFolderName);
    seedFilePath.append(File.separator);
    seedFilePath.append(seedFilename);

    URL fileURL = DatabaseSeeder.class.getClassLoader().getResource(seedFilePath.toString());

    try {
      if (fileURL != null) {
        Class entitiesTemplateClass = (Class) seedsMap.get(seedFilename);
        InputStream inputStream = DatabaseSeeder.class.getClassLoader().getResourceAsStream(seedFilePath.toString());

        JSONTemplateInterface entitiesTemplate =
            (JSONTemplateInterface) mapper.readValue(inputStream, entitiesTemplateClass);
        List<?> entities = entitiesTemplate.getEntities();
        inputStream.close();

        return entities;
      }

    } catch (com.fasterxml.jackson.core.JsonParseException e) {
      e.printStackTrace();
    } catch (JsonMappingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }


  /**
   * Retrieve a user from seeds by its role
   * @param seedFolder The seed folder from which to load users
   * @param role The role to select.
   * @return The first user with the role given is returned.
   */
  public static User getSeedUserByRole(String seedFolder, User.Role role) {
    List<User> seedUsers =
        (List<User>) DatabaseSeeder.getEntitiesListFromSeed(seedFolder, "users.json");
    return seedUsers
        .stream()
        .filter(user -> user.getRole().equals(role))
        .collect(Collectors.toList())
        .get(0);
  }


  /**
   * Ensure at least one admin user is preset. If not, this method creates it
   * as amin@email.com:admin
   *
   */
  public static void ensureAdminUserPresent() {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    Optional<User> optAdminUser = DatabaseHandler.fetchEntityBy(User.class, User_.role, User.Role.ADMINISTRATOR, session);

    if (!optAdminUser.isPresent()) {
      // create new admin user
      User defaultAdminUser = new User();
      defaultAdminUser.setRole(User.Role.ADMINISTRATOR);
      defaultAdminUser.setName("Admin");
      defaultAdminUser.setSurname("Admin surname");
      defaultAdminUser.setEmail("admin@email.com");
      defaultAdminUser.setPassword("admin");

      // save the created user
      session.persist(defaultAdminUser);
    }

    session.getTransaction().commit();
    session.close();
  }
}

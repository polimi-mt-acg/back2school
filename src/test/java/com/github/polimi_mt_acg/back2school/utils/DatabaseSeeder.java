package com.github.polimi_mt_acg.back2school.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.model.DeserializeToPersistInterface;
import com.github.polimi_mt_acg.utils.json_mappers.*;
import org.hibernate.Session;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DatabaseSeeder {

    private final static Logger LOGGER =
            Logger.getLogger(DatabaseSeeder.class.getName());

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
         * [x] payment
         * [x] authentication_session
         */
        // zero dependencies from other entities
        map.put("classrooms.json", ClassroomsJSONTemplate.class);
        map.put("subjects.json", SubjectsJSONTemplate.class);
        map.put("users.json", UsersJSONTemplate.class);

        // one or more dependency from other entities
        map.put("payments.json", PaymentsJSONTemplate.class);
        map.put("grades.json", GradesJSONTemplate.class);
        map.put("classes.json", ClassesJSONTemplate.class);
//        map.put("lectures.json", LecturesJSONTemplate.class);
//        map.put("notifications_general.json", NotificationsGeneralJSONTemplate.class);
//        map.put("appointments.json", AppointmentsJSONTemplate.class);
//        map.put("notifications_personal_parent.json", NotificationsPersonalParentJSONTemplate.class);
//        map.put("notifications_personal_teacher.json", NotificationsPersonalTeacherJSONTemplate.class);
//        map.put("notifications_class_parent.json", NotificationsClassParent5JSONTemplate.class);
//        map.put("notifications_class_teacher.json", NotificationsClassTeacherJSONTemplate.class);
//        map.put("notifications_read.json", NotificationsReadJSONTemplate.class);
//        map.put("authentication_sessions.json", AuthenticationSessionJSONTemplate.class);

        seedsMap = Collections.unmodifiableMap(map);
    }


    /**
     * Deploy new seeds to the database by specifying the folder name in order
     * load a scenario.
     *
     * E.g. DatabaseSeeder.deployScenario("scenarioA")
     * It will look into the folder src/test/resources/scenarios_seeds/scenarioA/
     * for Json files from which to load the data.
     *
     * @param scenarioFolderName
     */
    public static void deployScenario(String scenarioFolderName) {
        for (Map.Entry<String, Object> sm: seedsMap.entrySet()) {
            String seedFilename = sm.getKey();

            List<?> entitiesToPersist =
                    getEntitiesListFromSeed(scenarioFolderName, seedFilename);

            if (entitiesToPersist != null) {
                Session s = DatabaseHandler.getInstance().getNewSession();
                s.beginTransaction();
                for (Object genericEntity: entitiesToPersist) {
                    if (genericEntity instanceof DeserializeToPersistInterface) {

                        // cast the entity
                        DeserializeToPersistInterface entity =
                                (DeserializeToPersistInterface) genericEntity;

                        // notify the entity that it will be persisted
                        entity.prepareToPersist();

                        s.persist(entity);
                    }
                }
                s.getTransaction().commit();
                s.close();

                LOGGER.info(
                        String.format(
                                "deployed seed file: %s/%s",
                                scenarioFolderName,
                                seedFilename
                        )
                );
            }

        }
    }

    public static List<?> getEntitiesListFromSeed(String scenarioFolderName, String seedFilename) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);

        String seedFilePath = "src/test/resources/scenarios_seeds/"
                + scenarioFolderName
                + "/"
                + seedFilename;
        File f = new File(seedFilePath);

        try {
            if (f.exists() && !f.isDirectory()) {
                Class entitiesTemplateClass = (Class) seedsMap.get(seedFilename);

                JSONTemplateInterface entitiesTemplate =
                        (JSONTemplateInterface) mapper.readValue(f, entitiesTemplateClass);

                return entitiesTemplate.getEntities();
            }

        }
        catch (com.fasterxml.jackson.core.JsonParseException e) {
            e.printStackTrace();
        }
        catch (JsonMappingException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

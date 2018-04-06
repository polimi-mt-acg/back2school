package com.github.polimi_mt_acg.utils;


import com.github.polimi_mt_acg.back2school.model.DeserializeToPersistInterface;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.utils.json_mappers.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.hibernate.Session;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

        // zero dependencies from other entities
        map.put("classrooms.json", ClassroomsJSONTemplate.class);
        map.put("subjects.json", SubjectsJSONTemplate.class);
        map.put("users.json", UsersJSONTemplate.class);

        // one or more dependency from other entities
        map.put("classes.json", ClassesJSONTemplate.class);
        map.put("grades.json", GradesJSONTemplate.class);

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
        Gson gson = new Gson();

        String seedFilePath = "src/test/resources/scenarios_seeds/"
                + scenarioFolderName
                + "/"
                + seedFilename;
        File f = new File(seedFilePath);

        try {
            if (f.exists() && !f.isDirectory()) {
                JsonReader reader =
                        new JsonReader(new FileReader(seedFilePath));

                Class entitiesTemplateClass = (Class) seedsMap.get(seedFilename);
                JSONTemplateInterface entitiesTemplate =
                        gson.fromJson(reader, entitiesTemplateClass);

                return entitiesTemplate.getEntities();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}

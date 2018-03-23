package com.github.polimi_mt_acg.utils;


import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.github.polimi_mt_acg.back2school.model.DeserializeToPersistInterface;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.utils.json_mapper.ClassesJSONTemplate;
import com.github.polimi_mt_acg.utils.json_mapper.ClassroomsJSONTemplate;
import com.github.polimi_mt_acg.utils.json_mapper.JSONTemplateInterface;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.hibernate.Session;

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

        map.put("classrooms.json", ClassroomsJSONTemplate.class);
        map.put("classes.json", ClassesJSONTemplate.class);

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
        String scenarioBasePath =
                "src/test/resources/scenarios_seeds/" + scenarioFolderName + "/";

        try {
            Gson gson = new Gson();

            for (Map.Entry<String, Object> sm: seedsMap.entrySet()) {

                // create the base path for the seed file
                String seedFilePath = scenarioBasePath + sm.getKey();
                LOGGER.info(seedFilePath);


                JsonReader reader =
                        new JsonReader(new FileReader(seedFilePath));

                JSONTemplateInterface entitiesTemplate =
                        gson.fromJson(reader, (Class) sm.getValue());

                List<?> entitiesToPerist = entitiesTemplate.getEntities();


                Session s = DatabaseHandler.getInstance().getNewSession();
                s.beginTransaction();

                for (Object genericEntity: entitiesToPerist) {
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
            }

//            String filename = "src/test/resources/scenarios_seeds/scenarioA/classrooms.json";
//            Gson gson = new Gson();
//            JsonReader reader = new JsonReader(new FileReader(filename));
//            ClassroomsJSONTemplate data = gson.fromJson(reader, ClassroomsJSONTemplate.class);
//
//            Session s = DatabaseHandler.getInstance().getNewSession();
//            s.beginTransaction();
//
//            for (Classroom classroom: data.classrooms) {
//                LOGGER.info("CLASS NAME: " + classroom.getName());
//                s.persist(classroom);
//            }
//
//
//            s.getTransaction().commit();
//            s.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

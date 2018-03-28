package com.github.polimi_mt_acg.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Classroom;

import java.util.List;

public class ClassroomsJSONTemplate implements JSONTemplateInterface {

    public List<Classroom> classrooms;

    @Override
    public List<?> getEntities() {
        return classrooms;
    }
}

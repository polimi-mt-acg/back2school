package com.github.polimi_mt_acg.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.SchoolClass;

import java.util.List;

public class ClassesJSONTemplate implements JSONTemplateInterface {

    public List<SchoolClass> classes;

    @Override
    public List<?> getEntities() {
        return classes;
    }
}

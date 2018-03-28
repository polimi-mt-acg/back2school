package com.github.polimi_mt_acg.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.User;

import java.util.List;

public class UsersJSONTemplate implements JSONTemplateInterface {

    public List<User> users;

    @Override
    public List<?> getEntities() {
        return users;
    }
}

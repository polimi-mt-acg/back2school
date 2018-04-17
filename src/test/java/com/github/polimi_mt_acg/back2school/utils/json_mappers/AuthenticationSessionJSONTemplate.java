package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;

import java.util.List;

public class AuthenticationSessionJSONTemplate implements JSONTemplateInterface {

    public List<AuthenticationSession> authenticationSessions;

    @Override
    public List<?> getEntities() {
        return authenticationSessions;
    }
}

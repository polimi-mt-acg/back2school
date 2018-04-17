package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.model.User;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AdministratorResponse {

    @XmlElement
    private List<User> administrators;

    public AdministratorResponse() {
    }

    public AdministratorResponse(List<User> administrators) {
        this.administrators = administrators;
    }

    public List<User> getAdministrators() {
        return administrators;
    }
}

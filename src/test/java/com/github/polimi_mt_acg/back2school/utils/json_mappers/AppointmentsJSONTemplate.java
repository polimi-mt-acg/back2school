package com.github.polimi_mt_acg.back2school.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Appointment;

import java.util.List;

public class AppointmentsJSONTemplate implements JSONTemplateInterface {

    public List<Appointment> appointments;

    @Override
    public List<?> getEntities() {
        return appointments;
    }
}

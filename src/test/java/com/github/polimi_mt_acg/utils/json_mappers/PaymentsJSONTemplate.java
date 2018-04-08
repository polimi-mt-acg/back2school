package com.github.polimi_mt_acg.utils.json_mappers;

import com.github.polimi_mt_acg.back2school.model.Payment;

import java.util.List;

public class PaymentsJSONTemplate implements JSONTemplateInterface {

    public List<Payment> payments;

    @Override
    public List<?> getEntities() {
        return payments;
    }
}

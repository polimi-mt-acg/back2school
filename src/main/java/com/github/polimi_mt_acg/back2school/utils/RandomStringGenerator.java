package com.github.polimi_mt_acg.back2school.utils;

import java.util.UUID;

public class RandomStringGenerator {
    public static String generateString() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replace("-", "");
    }
}

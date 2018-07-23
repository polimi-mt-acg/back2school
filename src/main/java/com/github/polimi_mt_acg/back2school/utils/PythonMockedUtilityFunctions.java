package com.github.polimi_mt_acg.back2school.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PythonMockedUtilityFunctions {

  public static String str(Object o) {
    return String.valueOf(o);
  }

  public static Boolean as_boolean(String str_bool) {
    return Boolean.getBoolean(str_bool);
  }

  public static Integer as_int(String str_int) {
    return Integer.parseInt(str_int);
  }

  public static Long as_long(String str_lng) {
    return Long.parseLong(str_lng);
  }

  public static Float as_float(String str_flt) {
    return Float.parseFloat(str_flt);
  }

  public static Double as_double(String str_bdl) {
    return Double.parseDouble(str_bdl);
  }

  public static void print(Object... objects) {
    StringBuilder sb = new StringBuilder();

    for (Object o: objects) {
      if (o == null
          || o instanceof String
          || o instanceof Boolean
          || o instanceof Integer
          || o instanceof Long
          || o instanceof Float
          || o instanceof Double) {
        sb.append(str(o));
      } else {
        sb.append(as_json(o));
      }
    }

    System.out.println(sb.toString());
  }

  public static String as_json(Object o) {
    try {
      return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "EXCEPTION while converting object to json";
  }
}

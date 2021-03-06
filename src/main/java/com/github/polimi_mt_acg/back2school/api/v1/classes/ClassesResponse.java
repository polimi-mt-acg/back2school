package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ClassesResponse {
  private List<Entity> classes = new ArrayList<>();

  public List<Entity> getClasses() {
    return classes;
  }

  public void setClasses(List<Entity> classes) {
    this.classes = classes;
  }

  public  static class Entity {

    private String name;

    @JsonProperty("academic_year")
    private int academicYear;

    @JsonProperty("url_class")
    private String urlClass;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getAcademicYear() {
      return academicYear;
    }

    public void setAcademicYear(int academicYear) {
      this.academicYear = academicYear;
    }

    public String getUrlClass() {
      return urlClass;
    }

    public void setUrlClass(String urlClass) {
      this.urlClass = urlClass;
    }
  }
}

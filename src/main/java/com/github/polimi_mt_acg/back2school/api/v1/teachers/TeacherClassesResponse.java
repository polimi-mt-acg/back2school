package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TeacherClassesResponse {
  private List<Entity> classes = new ArrayList<>();

  public List<Entity> getClasses() {
    return classes;
  }

  public void setClasses(List<Entity> classes) {
    this.classes = classes;
  }

  public static class Entity {
    private String name;

    @JsonProperty("academic_year")
    private int academicYear;

    @JsonProperty("url_class")
    private String urlClass;

    @JsonProperty("url_class_students")
    private String urlClassStudents;

    @JsonProperty("url_class_timetable")
    private String urlClassTimetable;

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

    public String getUrlClassStudents() {
      return urlClassStudents;
    }

    public void setUrlClassStudents(String urlClassStudents) {
      this.urlClassStudents = urlClassStudents;
    }

    public String getUrlClassTimetable() {
      return urlClassTimetable;
    }

    public void setUrlClassTimetable(String urlClassTimetable) {
      this.urlClassTimetable = urlClassTimetable;
    }
  }
}

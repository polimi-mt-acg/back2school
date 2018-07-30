package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TimetableResponse {
  private List<Entity> lectures = new ArrayList<>();

  @JsonProperty
  public List<Entity> getLectures() {
    return lectures;
  }

  @JsonProperty
  public void setLectures(List<Entity> lectures) {
    this.lectures = lectures;
  }

  public static class Entity {
    @JsonProperty("datetime_start")
    private String datetimeStart;

    @JsonProperty("datetime_end")
    private String datetimeEnd;

    @JsonProperty("url_classroom")
    private String urlClassroom;

    @JsonProperty("url_subject")
    private String urlSubject;

    public String getDatetimeStart() {
      return datetimeStart;
    }

    public void setDatetimeStart(String datetimeStart) {
      this.datetimeStart = datetimeStart;
    }

    public String getDatetimeEnd() {
      return datetimeEnd;
    }

    public void setDatetimeEnd(String datetimeEnd) {
      this.datetimeEnd = datetimeEnd;
    }

    public String getUrlClassroom() {
      return urlClassroom;
    }

    public void setUrlClassroom(String urlClassroom) {
      this.urlClassroom = urlClassroom;
    }

    public String getUrlSubject() {
      return urlSubject;
    }

    public void setUrlSubject(String urlSubject) {
      this.urlSubject = urlSubject;
    }
  }
}

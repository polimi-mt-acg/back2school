package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TeacherAppointmentsResponse {
  private List<Entity> appointments = new ArrayList<>();

  public List<Entity> getAppointments() {
    return appointments;
  }

  public void setAppointments(List<Entity> appointments) {
    this.appointments = appointments;
  }

  public static class Entity {

    private Integer id;

    @JsonProperty("datetime_start")
    private String datetimeStart;

    @JsonProperty("datetime_end")
    private String datetimeEnd;

    private String status;

    @JsonProperty("url_parent")
    private String urlParent;

    public Integer getId() {
      return id;
    }

    public void setId(Integer id) {
      this.id = id;
    }

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

    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    public String getUrlParent() {
      return urlParent;
    }

    public void setUrlParent(String urlParent) {
      this.urlParent = urlParent;
    }
  }
}

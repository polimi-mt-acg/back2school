package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public class ParentAppointmentsResponse {
  private List<URI> appointments;

  @JsonProperty
  public List<URI> getAppointments() {
    return appointments;
  }

  @JsonProperty public void setAppointments(List<URI> appointments) {
    this.appointments = appointments;
  }
}

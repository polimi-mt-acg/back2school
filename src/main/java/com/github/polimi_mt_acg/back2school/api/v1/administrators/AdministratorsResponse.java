package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class AdministratorsResponse {

  @JsonProperty
  private List<URI> administrators;

  public List<URI> getAdministrators() {
    return administrators;
  }

  public void setAdministrators(List<URI> administrators) {
    this.administrators = administrators;
  }
}

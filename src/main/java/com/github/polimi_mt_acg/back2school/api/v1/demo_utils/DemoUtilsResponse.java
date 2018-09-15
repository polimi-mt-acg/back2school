package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;

public class DemoUtilsResponse {

  @JsonProperty("url_deploy_demo_data")
  private URI urlDeployDemoData;

  @JsonProperty("url_empty_database")
  private URI urlEmptyDatabase;

  public DemoUtilsResponse() {}

  public URI getUrlDeployDemoData() {
    return urlDeployDemoData;
  }

  public void setUrlDeployDemoData(URI urlDeployDemoData) {
    this.urlDeployDemoData = urlDeployDemoData;
  }

  public URI getUrlEmptyDatabase() {
    return urlEmptyDatabase;
  }

  public void setUrlEmptyDatabase(URI urlEmptyDatabase) {
    this.urlEmptyDatabase = urlEmptyDatabase;
  }
}

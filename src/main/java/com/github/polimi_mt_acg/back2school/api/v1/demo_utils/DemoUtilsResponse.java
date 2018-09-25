package com.github.polimi_mt_acg.back2school.api.v1.demo_utils;

import java.net.URI;
import java.util.HashMap;

public class DemoUtilsResponse {

  private String information = "This utilities should be used for demo purposes. " +
      "Please, notice that a deploy of demo data (/demo-utils/deploy-demo-data) will also empty the database before the new data insertion. This will end the current session and reset the admin account data. A new login will be required.";

  private HashMap<String, URI> urls = new HashMap<>();

  public DemoUtilsResponse() {}

  public String getInformation() {
    return information;
  }

  public void setInformation(String information) {
    this.information = information;
  }

  public void setUrlDeployDemoData(URI uriDeployDemoData) {
    this.urls.put("deploy_demo_data", uriDeployDemoData);
  }

  public void setUrlEmptyDatabase(URI uriEmptyDatabase) {
    this.urls.put("empty_database", uriEmptyDatabase);
  }

  public void setUrlWhoAmI(URI uriWhoAmI) {
    this.urls.put("who_am_i", uriWhoAmI);
  }

  public HashMap<String, URI> getUrls() {
    return urls;
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.classes;

import java.net.URI;
import java.util.List;

public class ClassResponse {
  private List<URI> classes;

  public List<URI> getClasses() {
    return classes;
  }

  public void setClasses(List<URI> classes) {
    this.classes = classes;
  }
}

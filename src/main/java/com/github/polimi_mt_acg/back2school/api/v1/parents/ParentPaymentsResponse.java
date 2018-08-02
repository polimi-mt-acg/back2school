package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

public class ParentPaymentsResponse {
  private  List<URI> payments;

  @JsonProperty
  public List<URI> getPayments() {
    return payments;
  }

  @JsonProperty public void setPayments(List<URI> payments) {
    this.payments = payments;
  }
}

package com.github.polimi_mt_acg.back2school.api.v1.parents;

public class PostParentPaymentPayRequest {
  private boolean paid = false;

  public boolean isPaid() {
    return paid;
  }

  public void setPaid(boolean paid) {
    this.paid = paid;
  }
}

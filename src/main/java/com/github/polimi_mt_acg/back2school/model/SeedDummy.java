package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Dummy entity created in order to be able to create associations with entities that have circular
 * dependencies.
 */
@Entity
@Table(name = "seed_dummy")
public class SeedDummy implements DeserializeToPersistInterface {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @Override
  public void prepareToPersist() {}
}

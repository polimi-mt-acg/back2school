package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "subject")
public class Subject implements DeserializeToPersistInterface {

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @JsonIgnore
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public void prepareToPersist() {}

  /**
   * Test weak equality against another object. Attributes tested to be equal: title, description.
   *
   * @param obj The object to be compared.
   * @return true if weak equality holds.
   */
  public boolean weakEquals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!Subject.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final Subject other = (Subject) obj;

    // name
    if ((this.getName() == null)
        ? (other.getName() != null)
        : !this.getName().equals(other.getName())) return false;

    // description
    if ((this.getDescription() == null)
        ? (other.getDescription() != null)
        : !this.getDescription().equals(other.getDescription())) return false;

    return true;
  }
}

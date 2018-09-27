package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Entity
@Table(
    name = "subject",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Subject implements DeserializeToPersistInterface, ValidableRequest {

  @JsonIgnore @Transient private Response invalidPostResponse;
  @JsonIgnore @Transient private Response invalidPutResponse;

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

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getName() == null || getName().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: name"))
              .build();
      return false;
    }

    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(Subject.class, Subject_.name, getName());

    if (subjectOpt.isPresent()) {
      invalidPostResponse =
          Response.status(Response.Status.CONFLICT)
              .entity(
                  new StatusResponse(
                      Response.Status.CONFLICT, "A subject with this name already exist"))
              .build();
      return false;
    }

    return true;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPostResponse() {
    return invalidPostResponse;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPut(Integer id) {
    Optional<Subject> subjectOpt = DatabaseHandler.fetchEntityBy(Subject.class, Subject_.id, id);
    if (!subjectOpt.isPresent()) {
      invalidPutResponse =
          Response.status(Response.Status.NOT_FOUND)
              .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown subject id"))
              .build();
      return false;
    }

    if (getName() == null || getName().isEmpty()) {
      invalidPutResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: name"))
              .build();
      return false;
    }

    Optional<Subject> otherSubjectOpt =
        DatabaseHandler.fetchEntityBy(Subject.class, Subject_.name, getName());

    if (otherSubjectOpt.isPresent() && otherSubjectOpt.get().getId() != id) {
      invalidPostResponse =
          Response.status(Response.Status.CONFLICT)
              .entity(
                  new StatusResponse(
                      Response.Status.CONFLICT, "A subject with this name already exist"))
              .build();
      return false;
    }

    return true;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPutResponse() {
    return invalidPutResponse;
  }
}

package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
    name = "classroom",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "floor", "building"})})
public class Classroom implements DeserializeToPersistInterface, ValidableRequest {

  @JsonIgnore @Transient private Response invalidPostResponse;
  @JsonIgnore @Transient private Response invalidPutResponse;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "floor")
  private int floor;

  @Column(name = "building")
  private String building;

  @JsonIgnore
  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getFloor() {
    return floor;
  }

  public void setFloor(int floor) {
    this.floor = floor;
  }

  public String getBuilding() {
    return building;
  }

  public void setBuilding(String building) {
    this.building = building;
  }

  @Override
  public void prepareToPersist() {}

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

    if (getBuilding() == null || getBuilding().isEmpty()) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: building"))
              .build();
      return false;
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    CriteriaBuilder b = session.getCriteriaBuilder();

    CriteriaQuery<Classroom> criteria = b.createQuery(Classroom.class);
    Root<Classroom> r = criteria.from(Classroom.class);
    criteria.select(r);
    criteria.where(
        b.and(
            b.equal(r.get(Classroom_.name), getName()),
            b.equal(r.get(Classroom_.floor), getFloor()),
            b.equal(r.get(Classroom_.building), getBuilding())));

    List<Classroom> results = session.createQuery(criteria).getResultList();

    if (results.size() > 0) {
      // do already exists another classroom with same name, floor, building
      invalidPostResponse =
          Response.status(Response.Status.CONFLICT)
              .entity(
                  new StatusResponse(
                      Response.Status.CONFLICT,
                      "A classroom with this name, floor and building year already exist"))
              .build();
      session.getTransaction().commit();
      session.close();
      return false;
    }
    session.getTransaction().commit();
    session.close();

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
    Optional<Classroom> classroomOpt =
        DatabaseHandler.fetchEntityBy(Classroom.class, Classroom_.id, id);
    if (!classroomOpt.isPresent()) {
      invalidPutResponse =
          Response.status(Response.Status.NOT_FOUND)
              .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown classroom id"))
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

    if (getBuilding() == null || getBuilding().isEmpty()) {
      invalidPutResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: building"))
              .build();
      return false;
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    CriteriaBuilder b = session.getCriteriaBuilder();

    CriteriaQuery<Classroom> criteria = b.createQuery(Classroom.class);
    Root<Classroom> r = criteria.from(Classroom.class);
    criteria.select(r);
    criteria.where(
        b.and(
            b.equal(r.get(Classroom_.name), getName()),
            b.equal(r.get(Classroom_.floor), getFloor()),
            b.equal(r.get(Classroom_.building), getBuilding())));

    List<Classroom> results = session.createQuery(criteria).getResultList();

    if (results.size() > 0) {
      Classroom otherClassroom = results.get(0);
      if (otherClassroom.getId() != id) {
        invalidPutResponse =
            Response.status(Response.Status.CONFLICT)
                .entity(
                    new StatusResponse(
                        Response.Status.CONFLICT,
                        "A classroom with this name, floor and building year already exist"))
                .build();
        session.getTransaction().commit();
        session.close();
        return false;
      }
    }
    session.getTransaction().commit();
    session.close();

    return true;
  }

  @Override
  @JsonIgnore
  public Response getInvalidPutResponse() {
    return invalidPutResponse;
  }
}

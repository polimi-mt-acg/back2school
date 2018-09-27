package com.github.polimi_mt_acg.back2school.api.v1.classes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

public class ClassRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;
  @JsonIgnore private Response invalidPutResponse;

  private String name;

  @JsonProperty("academic_year")
  private Integer academicYear;

  @JsonProperty("students_ids")
  private List<Integer> studentsIds = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAcademicYear() {
    return academicYear;
  }

  public void setAcademicYear(Integer academicYear) {
    this.academicYear = academicYear;
  }

  public List<Integer> getStudentsIds() {
    return studentsIds;
  }

  public void setStudentsIds(List<Integer> studentsIds) {
    this.studentsIds = studentsIds;
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

    if (getAcademicYear() == null || getAcademicYear() == 0) {
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  new StatusResponse(
                      Response.Status.BAD_REQUEST, "Missing required attribute: academic_year"))
              .build();
      return false;
    }

    for (Integer studentId : getStudentsIds()) {
      // get student from db
      Optional<User> studentOpt = DatabaseHandler.fetchEntityBy(User.class, User_.id, studentId);
      if (!studentOpt.isPresent() || !studentOpt.get().getRole().equals(User.Role.STUDENT)) {
        invalidPostResponse =
            Response.status(Response.Status.BAD_REQUEST)
                .entity(
                    new StatusResponse(
                        Response.Status.BAD_REQUEST, "Unknown student with id: " + str(studentId)))
                .build();
        return false;
      }
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    CriteriaBuilder b = session.getCriteriaBuilder();

    CriteriaQuery<Class> criteria = b.createQuery(Class.class);
    Root<Class> r = criteria.from(Class.class);
    criteria.select(r);
    criteria.where(
        b.and(
            b.equal(r.get(Class_.name), getName()),
            b.equal(r.get(Class_.academicYear), getAcademicYear())));

    List<Class> results = session.createQuery(criteria).getResultList();

    if (results.size() > 0) {
      // do already exists another class with same name and year
      invalidPostResponse =
          Response.status(Response.Status.CONFLICT)
              .entity(
                  new StatusResponse(
                      Response.Status.CONFLICT,
                      "A class with this name and academic year already exist"))
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
    Optional<Class> classOpt = DatabaseHandler.fetchEntityBy(Class.class, Class_.id, id);
    if (!classOpt.isPresent()) {
      invalidPutResponse =
          Response.status(Response.Status.NOT_FOUND)
              .entity(new StatusResponse(Response.Status.NOT_FOUND, "Unknown class id"))
              .build();
      return false;
    }

    if (!isValidForPost()) {
      invalidPutResponse = invalidPostResponse;
      return false;
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    CriteriaBuilder b = session.getCriteriaBuilder();

    CriteriaQuery<Class> criteria = b.createQuery(Class.class);
    Root<Class> r = criteria.from(Class.class);
    criteria.select(r);
    criteria.where(
        b.and(
            b.equal(r.get(Class_.name), getName()),
            b.equal(r.get(Class_.academicYear), getAcademicYear())));

    List<Class> results = session.createQuery(criteria).getResultList();

    if (results.size() > 0) {
      Class otherClass = results.get(0);
      if (otherClass.getId() != id) {
        invalidPutResponse =
            Response.status(Response.Status.CONFLICT)
                .entity(
                    new StatusResponse(
                        Response.Status.CONFLICT,
                        "A class with this name and academic year already exist"))
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

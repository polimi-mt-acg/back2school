package com.github.polimi_mt_acg.back2school.api.v1.students;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.ValidableRequest;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.time.LocalDate;
import java.util.Optional;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;

public class StudentGradeRequest implements ValidableRequest {

  @JsonIgnore private Response invalidPostResponse;
  @JsonIgnore private Response invalidPutResponse;

  @JsonProperty("subject_id")
  private Integer subjectId;

  private LocalDate date;
  private String title;
  private double grade;

  public Integer getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Integer subjectId) {
    this.subjectId = subjectId;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public double getGrade() {
    return grade;
  }

  public void setGrade(double grade) {
    this.grade = grade;
  }

  @Override
  @JsonIgnore
  public boolean isValidForPost() {
    if (getSubjectId() == null) {
      print("Missing attribute: subject_id");
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Status.BAD_REQUEST, "Missing required attribute: subject_id"))
              .build();
      return false;
    }

    Optional<Subject> subjectOpt =
        DatabaseHandler.fetchEntityBy(Subject.class, Subject_.id, getSubjectId());

    if (!subjectOpt.isPresent()) {
      print("Unknown subject id");
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown required subject id"))
              .build();
      return false;
    }

    if (getDate() == null) {
      print("Missing attribute: date");
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Status.BAD_REQUEST, "Missing required attribute: date"))
              .build();
      return false;
    }

    if (getTitle() == null || getTitle().isEmpty()) {
      print("Missing attribute: title");
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Status.BAD_REQUEST, "Missing required attribute: title"))
              .build();
      return false;
    }

    if (getGrade() == 0) {
      print("Missing attribute: grade");
      invalidPostResponse =
          Response.status(Response.Status.BAD_REQUEST)
              .entity(new StatusResponse(Status.BAD_REQUEST, "Missing required attribute: grade"))
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
    if (!isValidForPost()) {
      invalidPutResponse = invalidPostResponse;
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

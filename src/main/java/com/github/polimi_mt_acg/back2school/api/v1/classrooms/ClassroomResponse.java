package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

import com.github.polimi_mt_acg.back2school.model.Classroom;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /administrators REST API. The serialized JSON format has the
 * following structure: <code>{
 *   "classrooms" : [ {
 *     "name" : "Name 1",
 *     "floor" : "floor 1",
 *   }, ...]
 * }</code>
 */
@XmlRootElement
public class ClassroomResponse {

  @XmlElement private List<Classroom> classrooms;

  /** Empty constructor. */
  public ClassroomResponse() {}

  /**
   * Construct an ClassroomResponse out of a List of subjects. No copy of {@code classrooms} is
   * performed.
   */
  public ClassroomResponse(List<Classroom> classrooms) {
    this.classrooms = classrooms;
  }

  @JsonProperty
  public List<Classroom> getClassrooms() {
    return classrooms;
  }

  @JsonProperty
  public void setClassrooms(List<Classroom> classrooms) {
    this.classrooms = classrooms;
  }
}

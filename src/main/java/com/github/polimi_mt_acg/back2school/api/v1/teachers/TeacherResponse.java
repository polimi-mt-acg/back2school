package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.model.User;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /teachers REST API. The serialized JSON format has the
 * following structure: <code>{
 *   "teachers" : [ {
 *     "role" : "TEACHER",
 *     "name" : "Name 1",
 *     "surname" : "Surname 1",
 *     "email" : "namesurname@mail.com"
 *   }, ...]
 * }</code>
 */
@XmlRootElement
public class TeacherResponse {

  @XmlElement private List<User> teachers;

  /** Empty constructor. */
  public TeacherResponse() {}

  /**
   * Construct a TeacherResponse out of a List of teachers. No copy of {@code
   * teachers} is performed.
   */
  public TeacherResponse(List<User> teachers) {
    this.teachers = teachers;
  }

  public List<User> getTeachers() {
    return teachers;
  }
}

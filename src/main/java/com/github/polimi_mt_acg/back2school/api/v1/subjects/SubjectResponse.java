package com.github.polimi_mt_acg.back2school.api.v1.subjects;

import com.github.polimi_mt_acg.back2school.model.Notification;
import com.github.polimi_mt_acg.back2school.model.Subject;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /administrators REST API. The serialized JSON format has the
 * following structure: <code>{
 *   "subjects" : [ {
 *     "name" : "Name 1",
 *     "description" : "Description 1",
 *   }, ...]
 * }</code>
 */
@XmlRootElement
public class SubjectResponse {

  @XmlElement private List<Subject> subjects;

  /** Empty constructor. */
  public SubjectResponse() {}

  /**
   * Construct an SubjectResponse out of a List of subjects. No copy of {@code subjects} is
   * performed.
   */
  public SubjectResponse(List<Subject> subjects) {
    this.subjects = subjects;
  }

  @JsonProperty
  public List<Subject> getSubjects() {
    return subjects;
  }

  @JsonProperty
  public void setSubjects(List<Subject> subjects) {
    this.subjects = subjects;
  }
}

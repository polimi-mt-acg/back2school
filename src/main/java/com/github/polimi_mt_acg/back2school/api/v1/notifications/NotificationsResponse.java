package com.github.polimi_mt_acg.back2school.api.v1.notifications;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.model.Notification;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /notifications REST API. The serialized JSON format has the
 * following structure: <code>{
 *   "notifications" : [ {
 *     "role" : "ADMINISTRATOR",
 *     "name" : "Name 1",
 *     "surname" : "Surname 1",
 *     "email" : "namesurname@mail.com"
 *   }, ...]
 * }</code>
 */
@XmlRootElement
public class NotificationsResponse {

  @XmlElement private List<Notification> notifications;

  /** Empty constructor. */
  public NotificationsResponse() {}

  /**
   * Construct an AdministratorResponse out of a List of notifications. No copy of {@code
   * notifications} is performed.
   */
  public NotificationsResponse(List<Notification> notifications) {
    this.notifications = notifications;
  }

  @JsonProperty public List<Notification> getNotifications() {
    return notifications;
  }

  @JsonProperty public void setNotifications(
      List<Notification> notifications) {
    this.notifications = notifications;
  }
}

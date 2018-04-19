package com.github.polimi_mt_acg.back2school.api.v1.administrators;

import com.github.polimi_mt_acg.back2school.model.User;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The response to a request to /administrators REST API. The serialized JSON format has the
 * following structure: <code>{
 *   "administrators" : [ {
 *     "role" : "ADMINISTRATOR",
 *     "name" : "Name 1",
 *     "surname" : "Surname 1",
 *     "email" : "namesurname@mail.com"
 *   }, ...]
 * }</code>
 */
@XmlRootElement
public class AdministratorResponse {

  @XmlElement private List<User> administrators;

  /** Empty constructor. */
  public AdministratorResponse() {}

  /**
   * Construct an AdministratorResponse out of a List of administrators. No copy of {@code
   * administrators} is performed.
   */
  public AdministratorResponse(List<User> administrators) {
    this.administrators = administrators;
  }

  /** @return The iterator to the administrators returned. */
  public Iterator<User> getAdministrators() {
    return administrators.iterator();
  }
}

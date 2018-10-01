package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Lecture;
import com.github.polimi_mt_acg.back2school.model.Lecture_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import org.hibernate.Session;

import static com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecurityContext.getPathParameter;

/**
 * TeacherOfStudentSecurityContext implements a request filter for JAX-RS REST APIs.
 *
 * <p>It implements a "TeacherOfAccessedStudent-only" security policy.
 *
 * <p>A REST API annotated with this, can only be accessed if:
 *
 * <p>a) the client is authenticated.
 *
 * <p>b) if client role is TEACHER then the user is required to be a teacher of the accessed student
 *
 * <p>To be successfully authenticated, the client must send the auth token in the AUTHORIZATION
 * HTTP header.
 */
@TeacherOfStudentSecured
@Provider
@Priority(SecurityContextPriority.TEACHER_OF_STUDENT)
public class TeacherOfStudentSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext)
      throws InvalidTemplateParameterException {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    User currentUser = AuthenticationSession.getCurrentUser(requestContext, session);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (currentUser == null) {
      session.getTransaction().commit();
      session.close();
      requestContext.abortWith(
          Response.status(Response.Status.UNAUTHORIZED)
              .entity(new StatusResponse(Response.Status.UNAUTHORIZED, "Invalid session"))
              .build());
      return;
    }

    // if the user logged in is not on the checked role
    if (!currentUser.getRole().equals(Role.TEACHER)) {
      session.getTransaction().commit();
      session.close();
      return; // Move control to next filter
    }
    // here currentUser has role TEACHER

    // get studentId parameter from path
    Integer studentId = getPathParameter("studentId", requestContext);

    List<Lecture> lectures =
        dbi.getListSelectFromWhereEqual(Lecture.class, Lecture_.teacher, currentUser, session);
    for (Lecture lecture : lectures) {
      for (User student : lecture.getClass_().getClassStudents()) {
        // For all the lectures of the teacher, get the students of the class.
        // If a student matches with the studentId, then 'teacher' is a teacher of student.
        if (student.getId() == studentId) {
          session.getTransaction().commit();
          session.close();
          return; // Move control to next filter
        }
      }
    }

    // if arrived here, not a teacher of student
    session.getTransaction().commit();
    session.close();
    requestContext.abortWith(
        Response.status(Status.FORBIDDEN)
            .entity(new StatusResponse(Status.FORBIDDEN, "Access to the resource not allowed"))
            .build());
  }
}

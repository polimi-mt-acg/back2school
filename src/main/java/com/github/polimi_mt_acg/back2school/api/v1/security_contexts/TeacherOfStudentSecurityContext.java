package com.github.polimi_mt_acg.back2school.api.v1.security_contexts;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.InvalidTemplateParameterException;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.SecurityContextPriority;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherOfStudentSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.Lecture;
import com.github.polimi_mt_acg.back2school.model.Lecture_;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import java.io.IOException;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.hibernate.Session;

@TeacherOfStudentSecured
@Provider
@Priority(SecurityContextPriority.TEACHER_OF_STUDENT)
public class TeacherOfStudentSecurityContext implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();
    User teacher = AuthenticationSession.getCurrentUser(requestContext, session);

    // Abort the filter chain with a 401 status code response

    // if there is no user logged in
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
      return;
    }

    // if the user logged has not the correct role
    if (teacher.getRole() != Role.TEACHER) {
      session.getTransaction().commit();
      session.close();
      return; // Move control to next filter
    }

    // Get the student id template parameter
    UriInfo uriInfo = requestContext.getUriInfo();
    MultivaluedMap<String, String> pathParameter = uriInfo.getPathParameters();

    if (!pathParameter.containsKey("id")) {
      session.getTransaction().commit();
      session.close();
      throw new InvalidTemplateParameterException(
          "Could not find 'id' template parameter in URI. Maybe you annotated"
              + "a non /students/{id}/* REST endpoint?");
    }

    // Get student id from request's URI
    int studentId = Integer.parseInt(pathParameter.getFirst("id"));

    List<User> students =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.id, studentId, session);
    if (students.isEmpty()) {
      session.getTransaction().commit();
      session.close();
      requestContext.abortWith(Response.status(Status.NOT_FOUND).build());
    }

    // Check is requested student is in a class of requesting teacher
    User student = students.get(0);

    List<Lecture> lectures =
        dbi.getListSelectFromWhereEqual(Lecture.class, Lecture_.teacher, teacher, session);
    boolean isTeacherOfStudent = false;
    for (Lecture lecture : lectures) {
      for (User s : lecture.getClass_().getClassStudents()) {
        // For all the lectures of the teacher, get the class C. If student is in C, then 'teacher'
        // is a teacher of student.
        if (s == student) {
          isTeacherOfStudent = true;
          break;
        }
      }
    }

    session.getTransaction().commit();
    session.close();

    if (!isTeacherOfStudent) {
      requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
    }
  }
}

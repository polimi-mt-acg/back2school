package com.github.polimi_mt_acg.back2school.api.v1.teachers;

import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.TeacherAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.AuthenticationSession;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.model.User_;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import org.hibernate.Session;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

/** JAX-RS Resource for teachers entity. */
@Path("teachers")
public class TeacherResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public TeacherResponse getTeachers() {
    List<User> teachers =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, User.Role.TEACHER);

    return new TeacherResponse(teachers);
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postTeachers(PostTeacherRequest request, @Context UriInfo uriInfo) {
    User teacher = request.getTeacher();

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Check if input user is a teacher
    if (teacher.getRole() != User.Role.TEACHER) {
      return Response.status(Status.BAD_REQUEST)
          .entity("Provided user error: not a teacher")
          .build();
    }

    // Check if a user with same email already exists, if so, do nothing
    Optional<User> result =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, teacher.getEmail());
    if (result.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity("An account with this email already exists.")
          .build();
    }

    teacher.prepareToPersist();

    session.persist(teacher);
    session.getTransaction().commit();
    session.close();

    // Now the teacher has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(String.valueOf(teacher.getId())).build();
    return Response.created(uri).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @TeacherAdministratorSecured
  public Response getTeacherById(@PathParam("id") String id, @Context ContainerRequestContext crc) {
    User currentUser = AuthenticationSession.getCurrentUser(crc);

    // Fetch User
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, Integer.parseInt(id));
    if (!userOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("User not found").build();
    }
    User teacher = userOpt.get();

    if (currentUser.getRole().equals(Role.TEACHER) && currentUser.getId() != teacher.getId()) {
      System.out.println("FORBIDDEN HERE");
      return Response.status(Status.FORBIDDEN).entity("Not allowed user").build();
    }

    return Response.ok(teacher, MediaType.APPLICATION_JSON_TYPE).build();
  }
}

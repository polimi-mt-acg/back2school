package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
import com.github.polimi_mt_acg.back2school.api.v1.notifications.NotificationsResponse;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.AdministratorSecured;
import com.github.polimi_mt_acg.back2school.api.v1.security_contexts.ParentAdministratorSecured;
import com.github.polimi_mt_acg.back2school.model.*;
import com.github.polimi_mt_acg.back2school.model.Class;
import com.github.polimi_mt_acg.back2school.model.User.Role;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.str;

@Path("parents")
public class ParentsResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response getParents(@Context UriInfo uriInfo) {
    // Get parents from DB
    List<User> parents =
        DatabaseHandler.getInstance()
            .getListSelectFromWhereEqual(User.class, User_.role, Role.PARENT);

    // For each user, build a URI to /parents/{id}
    List<URI> uris = new ArrayList<>();
    for (User parent : parents) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(parent.getId())).build();
      uris.add(uri);
    }

    ParentsResponse response = new ParentsResponse();
    response.setParents(uris);
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParents(User newUser, @Context UriInfo uriInfo) {
    if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("User must have an email address.").build();
    }

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Check if a user with same email already exists, if so, do nothing
    Optional<User> userOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, newUser.getEmail(), session);

    if (userOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity(new StatusResponse(Status.CONFLICT, "A user with this email already exists"))
          .build();
    }
    // force to be a parent since this endpoint meaning
    newUser.setRole(Role.PARENT);
    newUser.prepareToPersist();

    session.persist(newUser);
    session.getTransaction().commit();
    session.close();

    // Now the teacher has the ID field filled by the ORM
    UriBuilder builder = uriInfo.getAbsolutePathBuilder();
    URI uri = builder.path(str(newUser.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentById(@PathParam("id") String parentId) {
    // Fetch User
    Optional<User> parentOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.id, Integer.parseInt(parentId));
    if (!parentOpt.isPresent() || !parentOpt.get().getRole().equals(Role.PARENT)) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    return Response.ok(parentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response putParentById(User newParent, @PathParam("id") String parentId) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Update student fields
    parent.setName(newParent.getName());
    parent.setSurname(newParent.getSurname());
    parent.setEmail(newParent.getEmail());
    parent.setNewPassword(newParent.getNewPassword());
    parent.prepareToPersist();

    session.getTransaction().commit();
    session.close();

    // According to HTTP specification:
    // HTTP status code 200 OK for a successful PUT of an update to an existing resource. No
    // response body needed.
    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/children")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentChildren(@PathParam("id") Integer parentId, @Context UriInfo uriInfo) {
    print("FROM HERE");
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch User
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    ParentChildrenResponse response = new ParentChildrenResponse();
    // force loading of entities -> avoid them to be lazy loaded after when
    // they're required for serialization (response) but session already closed
    parent.getChildren().size();
    response.setChildren(parent.getChildren());

    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/children")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParentChildren(
      ParentsChildrenRequest request, @PathParam("id") Integer parentId) {

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Fetch the parent
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      print("Unknown parent id: ", parentId);
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the child
    User newChild = session.get(User.class, request.getChildId());
    if (newChild == null) {
      print("Unknown child id: ", request.getChildId());
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown child id"))
          .build();
    }

    // Check if student is already a child of the parent
    for (User child : parent.getChildren()) {
      if (child.getId() == newChild.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(new StatusResponse(Status.CONFLICT, "Student already assigned to this parent"))
            .build();
      }
    }

    // add the student to the class
    parent.addChild(newChild);

    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/appointments")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentAppointments(
      @PathParam("id") String parentId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch Parent
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch appointments of parent
    List<Appointment> appointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);
    // ATT: parent
    ParentAppointmentsResponse response = new ParentAppointmentsResponse();
    List<URI> appointmentsURIs = new ArrayList<>();

    for (Appointment a : appointments) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(a.getId())).build();
      appointmentsURIs.add(uri);
    }
    response.setAppointments(appointmentsURIs);

    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/appointments")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response postParentAppointments(
      ParentAppointmentRequest request,
      @PathParam("id") Integer parentId,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent who made the request
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the teacher
    User teacher = session.get(User.class, request.getTeacherId());
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown teacher id"))
          .build();
    }

    // To check! Do we need to do these checks?
    // Get appointments of the teacher
    List<Appointment> teacherAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.teacher, teacher, session);

    // Is the teacher available in that time slot?
    for (Appointment a : teacherAppointments) {
      if ((a.getDatetimeStart().isBefore(request.getDatetimeStart())
              && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
          || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
              && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
              && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
              && a.getDatetimeEnd().isEqual(request.getDatetimeEnd()))) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Teacher has already an appointment in selected time slot."))
            .build();
      }
    }

    // Get appointments of the parent
    List<Appointment> parentAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);

    // Is the parent available in that time slot?
    for (Appointment a : parentAppointments) {
      if ((a.getDatetimeStart().isBefore(request.getDatetimeStart())
              && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
          || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
              && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
              && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
          || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
              && a.getDatetimeEnd().isEqual(request.getDatetimeEnd()))) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Parent has already an appointment in selected time slot."))
            .build();
      }
    }

    // Build the Appointment entity
    Appointment appointment = new Appointment();
    appointment.setParent(parent);
    appointment.setTeacher(teacher);
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
    // forced requested since created first time
    appointment.setStatus(Appointment.Status.REQUESTED);
    appointment.prepareToPersist();

    session.persist(appointment);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(appointment.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/appointments/{appointment_id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentAppointmentById(
      @PathParam("appointment_id") String appointmentId,
      @PathParam("id") String parentId,
      @Context UriInfo uriInfo) {

    // Fetch appointment
    Optional<Appointment> appointmentOpt =
        DatabaseHandler.fetchEntityBy(
            Appointment.class, Appointment_.id, Integer.parseInt(appointmentId));
    if (!appointmentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown appointment id"))
          .build();
    }

    if (appointmentOpt.get().getParent().getId() != Integer.parseInt(parentId)) {
      return Response.status(Status.CONFLICT)
          .entity(new StatusResponse(Status.CONFLICT, "Not current parent's appointment"))
          .build();
    }

    return Response.ok(appointmentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/appointments/{appointmentId: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentSecured
  @SameParentSecured
  public Response putParentAppointmentById(
      ParentAppointmentRequest request,
      @PathParam("id") Integer parentId,
      @PathParam("appointmentId") Integer appointmentId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent who made the request
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the teacher
    User teacher = session.get(User.class, request.getTeacherId());
    if (teacher == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(new StatusResponse(Status.BAD_REQUEST, "Unknown teacher id"))
          .build();
    }

    // Fetch Appointment entity
    Appointment appointment = session.get(Appointment.class, appointmentId);
    if (appointment == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown appointment id"))
          .build();
    }

    // Check if 'parent' is valid
    if (parent.getId() != appointment.getParent().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.BAD_REQUEST)
          .entity(
              new StatusResponse(
                  Status.BAD_REQUEST,
                  "Invalid parentId. It does not correspond to the previous one"))
          .build();
    }

    // Check if 'teacher' is valid
    if (teacher.getId() != appointment.getTeacher().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.notModified()
          .entity(
              new StatusResponse(
                  Status.BAD_REQUEST,
                  "Invalid parentId. It does not correspond to the previous one"))
          .build();
    }

    // Checks like in POST! But we don't check over the modified appointment!

    // Get appointments of the teacher
    List<Appointment> teacherAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.teacher, teacher, session);
    // Is the teacher available in that time slot?
    for (Appointment a : teacherAppointments) {
      if (((a.getDatetimeStart().isBefore(request.getDatetimeStart())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
              || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
                  && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
                  && a.getDatetimeEnd().isEqual(request.getDatetimeEnd())))
          && a.getId() != appointment.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT,
                    "Teacher has already an appointment in the selected time slot."))
            .build();
      }
    }

    // Get appointments of the parent
    List<Appointment> parentAppointments =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);
    // Is the parent available in that time slot?
    for (Appointment a : parentAppointments) {
      if (((a.getDatetimeStart().isBefore(request.getDatetimeStart())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeStart()))
              || (a.getDatetimeStart().isAfter(request.getDatetimeStart())
                  && a.getDatetimeEnd().isBefore(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isBefore(request.getDatetimeEnd())
                  && a.getDatetimeEnd().isAfter(request.getDatetimeEnd()))
              || (a.getDatetimeStart().isEqual(request.getDatetimeStart())
                  && a.getDatetimeEnd().isEqual(request.getDatetimeEnd())))
          && a.getId() != appointment.getId()) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity(
                new StatusResponse(
                    Status.CONFLICT, "Parent has already an appointment in selected time slot."))
            .build();
      }
    }

    // Update appointment fields
    // appointment.setUser(parent);  is the the same
    // appointment.setTeacher(teacherOpt.get()); is the same
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
    appointment.setStatus(request.getStatus());

    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/payments")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentPayments(@PathParam("id") String parentId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch Parent
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch payments of parent
    List<Payment> payments =
        dbi.getListSelectFromWhereEqual(Payment.class, Payment_.assignedTo, parent, session);
    // ATT: parent
    ParentPaymentsResponse response = new ParentPaymentsResponse();
    List<URI> paymentsURIs = new ArrayList<>();

    for (Payment p : payments) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(p.getId())).build();
      paymentsURIs.add(uri);
    }
    response.setPayments(paymentsURIs);

    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/payments")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParentPayments(
      ParentPaymentRequest request,
      @PathParam("id") Integer parentId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders);

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Build the Payment entity
    Payment payment = new Payment();
    payment.setPlacedBy(currentUser);
    payment.setAssignedTo(parent);
    payment.setType(request.getType());
    payment.setDatetimeRequested(request.getDatetimeRequested());
    // due to the meaning of this endpoint
    payment.setDatetimeDone(null);
    payment.setDatetimeDeadline(request.getDatetimeDeadline());
    // due to the meaning of this endpoint
    payment.setDone(false);
    payment.setSubject(request.getSubject());
    payment.setDescription(request.getDescription());
    payment.setAmount(request.getAmount());
    payment.prepareToPersist();

    session.persist(payment);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(payment.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/payments/{payment_id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentPaymentById(
      @PathParam("id") Integer parentId,
      @PathParam("payment_id") Integer paymentId,
      @Context UriInfo uriInfo) {

    // Fetch payment
    Optional<Payment> paymentOpt =
        DatabaseHandler.fetchEntityBy(Payment.class, Payment_.id, paymentId);
    if (!paymentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown payment id"))
          .build();
    }
    Payment payment = paymentOpt.get();

    // check payment assigned to current parent (url id)
    if (payment.getAssignedTo().getId() != parentId) {
      return Response.status(Status.NOT_FOUND)
          .entity(
              new StatusResponse(
                  Status.NOT_FOUND, "No payment with this is for the current parent"))
          .build();
    }
    return Response.ok(payment, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/payments/{payment_id: [0-9]+}/pay")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentSecured
  @SameParentSecured
  public Response postParentPaymentPaid(
      String request, // ignored, not necessary
      @PathParam("id") Integer parentId,
      @PathParam("payment_id") Integer paymentId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    // The only user allowed to invoke this endpoint is the user who has to pay

    User parent = AuthenticationSession.getCurrentUser(httpHeaders);

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();
    // Get payment
    Payment payment = session.get(Payment.class, paymentId);
    if (payment == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown payment id"))
          .build();
    }

    if (parent.getId() != payment.getAssignedTo().getId()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT)
          .entity(new StatusResponse(Status.CONFLICT, "Wrong payment"))
          .build();
    }

    if (payment.isDone()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_MODIFIED)
          .entity(new StatusResponse(Status.NOT_MODIFIED, "Payment already confirmed"))
          .build();
    }

    payment.setDone(true);
    payment.setDatetimeDone(LocalDateTime.now());
    payment.prepareToPersist();

    session.persist(payment);
    session.getTransaction().commit();
    session.close();

    return Response.ok().entity(new StatusResponse(Status.OK)).build();
  }

  @Path("{id: [0-9]+}/notifications")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentNotifications(
      @PathParam("id") Integer parentId, @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch Parent
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch notification for the parent
    // Three possible notifications type for the parent:

    // 1:General Parents
    List<NotificationGeneralParents> notificationsGP =
        dbi.getListSelectFrom(NotificationGeneralParents.class);

    // 2:Class Parent
    // Initialized an empty NotificationClassParent list
    List<NotificationClassParent> notificationsCP = new ArrayList<>();
    List<Class> classes = dbi.getListSelectFrom(Class.class);

    for (User child : parent.getChildren()) {
      for (Class cl : classes) {
        for (User student : cl.getClassStudents()) {
          if (student.getId() == child.getId()) {
            notificationsCP.addAll(
                dbi.getListSelectFromWhereEqual(
                    NotificationClassParent.class,
                    NotificationClassParent_.targetClass,
                    cl,
                    session));
            break;
          }
        }
      }
    }

    // 3:Personal Parent
    List<NotificationPersonalParent> notificationPP =
        dbi.getListSelectFromWhereEqual(
            NotificationPersonalParent.class,
            NotificationPersonalParent_.targetUser,
            parent,
            session);

    NotificationsResponse response = new NotificationsResponse();
    response.setNotifications(new ArrayList<>());

    for (NotificationGeneralParents ngp : notificationsGP) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(ngp.getId())).build();
      response.getNotifications().add(uri);
    }

    for (NotificationClassParent ncp : notificationsCP) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(ncp.getId())).build();
      response.getNotifications().add(uri);
    }

    for (NotificationPersonalParent npp : notificationPP) {
      URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(npp.getId())).build();
      response.getNotifications().add(uri);
    }
    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/notifications")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParentNotifications(
      NotificationPersonalParent npp,
      @PathParam("id") Integer parentId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {
    // Here the admin can POST only a direct notification to this parent
    User currentAdmin = AuthenticationSession.getCurrentUser(httpHeaders);

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // Get parent target of the notification
    User parent = session.get(User.class, parentId);
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    npp.setCreator(currentAdmin);
    npp.setTargetUser(parent);
    npp.setDatetime(LocalDateTime.now());
    npp.prepareToPersist();

    session.persist(npp);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(npp.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/notifications/{notificationId: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentNotificationById(
      @PathParam("id") Integer parentId,
      @PathParam("notificationId") Integer notificationId,
      @Context HttpHeaders httpHeaders,
      @Context UriInfo uriInfo) {

    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();
    User currentUser = AuthenticationSession.getCurrentUser(httpHeaders, session);

    // Notification can be of three types: GeneralParents, ClassParent, PersonalParent
    // if none of them will be found by the given id -> error

    // 1: GeneralParents
    NotificationGeneralParents notificationGeneralParents =
        session.get(NotificationGeneralParents.class, notificationId);
    if (notificationGeneralParents != null) {
      // mark as read
      if (currentUser.getRole().equals(Role.PARENT)) {
        currentUser.addNotificationsRead(notificationGeneralParents);
      }
      session.getTransaction().commit();
      session.close();
      return Response.ok(notificationGeneralParents, MediaType.APPLICATION_JSON_TYPE).build();
    }

    // 2: ClassParent
    NotificationClassParent notificationClassParent =
        session.get(NotificationClassParent.class, notificationId);
    if (notificationClassParent != null) {
      // Parent logged in
      if (currentUser.getRole().equals(Role.PARENT)) {
        // look for a children of the current parent belonging the class
        for (User student : notificationClassParent.getTargetClass().getClassStudents()) {
          for (User child : currentUser.getChildren()) {
            if (student.getId() == child.getId()) {
              // found matching class student and children of the parent

              // mark as read
              currentUser.addNotificationsRead(notificationClassParent);

              session.getTransaction().commit();
              session.close();
              notificationClassParent.setTargetClass(null); // so won't be serialized
              return Response.ok(notificationClassParent, MediaType.APPLICATION_JSON_TYPE).build();
            }
          }
        }

      } // Administrator logged in
      else if (currentUser.getRole().equals(Role.ADMINISTRATOR)) {
        session.getTransaction().commit();
        session.close();
        notificationClassParent.setTargetClass(null); // so won't be serialized
        return Response.ok(notificationClassParent, MediaType.APPLICATION_JSON_TYPE).build();
      }
    }

    // 3: PersonalParent
    NotificationPersonalParent notificationPersonalParent =
        session.get(NotificationPersonalParent.class, notificationId);
    if (notificationPersonalParent != null) {
      // mark as read
      if (currentUser.getRole().equals(Role.PARENT)) {
        currentUser.addNotificationsRead(notificationPersonalParent);
      }
      session.getTransaction().commit();
      session.close();
      notificationPersonalParent.setTargetUser(null); // so won't be serialized
      return Response.ok(notificationPersonalParent, MediaType.APPLICATION_JSON_TYPE).build();
    }

    session.getTransaction().commit();
    session.close();
    return Response.status(Status.NOT_FOUND)
        .entity(
            new StatusResponse(
                Status.NOT_FOUND,
                "No notification matching parent id and notification id was found"))
        .build();
  }
}

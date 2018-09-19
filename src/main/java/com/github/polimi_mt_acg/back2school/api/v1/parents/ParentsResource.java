package com.github.polimi_mt_acg.back2school.api.v1.parents;

import com.github.polimi_mt_acg.back2school.api.v1.StatusResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.hibernate.Session;

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
    if (!parentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }
    User parent = parentOpt.get();

    return Response.ok(parent, MediaType.APPLICATION_JSON_TYPE).build();
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
  public Response getParentChildren(@PathParam("id") String parentId, @Context UriInfo uriInfo) {
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

    ParentChildrenResponse response = new ParentChildrenResponse();
    response.setChildren(parent.getChildren());

    List<URI> childrenURI = new ArrayList<>();

    for (User c : parent.getChildren()) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(c.getId())).build();
      childrenURI.add(uri);
    }

    session.getTransaction().commit();
    session.close();
    return Response.ok(response, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/children")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @AdministratorSecured
  public Response postParentChildren( // is postParentChild a better name?
      PostChildrenRequest request,
      @PathParam("id") String parentId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch parent
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the student entity by name
    Optional<User> studentOpt =
        DatabaseHandler.fetchEntityBy(
            User.class, User_.email, request.getStudent().getEmail(), session);
    if (!studentOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown student mail"))
          .build();
    }
    // Check if student is already a child of the parent
    for (User child : parent.getChildren()) {
      if (child.getEmail().equals(studentOpt.get().getEmail())) {
        session.getTransaction().commit();
        session.close();
        return Response.status(Status.CONFLICT)
            .entity("Student already assigned to this parent")
            .build();
      }
    }

    // Add the new children to the parent
    parent.addChild(request.getStudent());

    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
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
      @PathParam("id") String parentId,
      PostParentAppointmentRequest request,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent who made the request
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the teacher entity by email
    Optional<User> teacherOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, request.getTeacherEmail(), session);
    if (!teacherOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher name"))
          .build();
    }

    // To check! Do we need to do these checks?
    // Get appointments of the teacher
    List<Appointment> resultTeacher =
        dbi.getListSelectFromWhereEqual(
            Appointment.class, Appointment_.teacher, teacherOpt.get(), session);

    // Is the teacher available in that time slot?
    for (Appointment a : resultTeacher) {
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
                    Status.CONFLICT, "Teacher has already an appointment in that time slot."))
            .build();
      }
    }

    // Get appointments of the parent
    List<Appointment> resultParent =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);

    // Is the parent available in that time slot?
    for (Appointment a : resultParent) {
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
                    Status.CONFLICT, "Parent has already an appointment in that time slot."))
            .build();
      }
    }

    // Build the Appointment entity
    Appointment appointment = new Appointment();
    appointment.setParent(parent);
    appointment.setTeacher(teacherOpt.get());
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());
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

  @Path("{id: [0-9]+}/appointments/{appointment_id: [0-9]+}")
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @ParentSecured
  @SameParentSecured
  public Response putParentAppointmentById(
      PostParentAppointmentRequest request,
      @PathParam("id") String parentId,
      @PathParam("appointment_id") String appointmentId,
      @Context ContainerRequestContext crc,
      @Context UriInfo uriInfo) {
    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Fetch parent
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown parent id"))
          .build();
    }

    // Fetch the teacher entity by email
    Optional<User> teacherOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, request.getTeacherEmail(), session);
    if (!teacherOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher name"))
          .build();
    }

    // Fetch Appointment entity
    Appointment appointment = session.get(Appointment.class, Integer.parseInt(appointmentId));
    if (appointment == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown appointment id"))
          .build();
    }

    // Check if 'parent' is the same that created the Appointment entity
    if (!parent.getEmail().equals(appointment.getParent().getEmail())) {
      session.getTransaction().commit();
      session.close();
      return Response.notModified().entity("You cannot modify this appointment.").build();
    }

    // Check if 'parent' is the same that created the Appointment entity
    if (!teacherOpt.get().getEmail().equals(appointment.getTeacher().getEmail())) {
      session.getTransaction().commit();
      session.close();
      return Response.notModified().entity("You cannot modify this appointment.").build();
    }

    // Checks like in POST!But we don't check over the modified appointment!

    // Get appointments of the teacher
    List<Appointment> resultTeacher =
        dbi.getListSelectFromWhereEqual(
            Appointment.class, Appointment_.teacher, teacherOpt.get(), session);
    // Is the teacher available in that time slot?
    for (Appointment a : resultTeacher) {
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
                    Status.CONFLICT, "Teacher has already an appointment in that time slot."))
            .build();
      }
    }

    // Get appointments of the parent
    List<Appointment> resultParent =
        dbi.getListSelectFromWhereEqual(Appointment.class, Appointment_.parent, parent, session);
    // Is the parent available in that time slot?
    for (Appointment a : resultParent) {
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
                    Status.CONFLICT, "Parent has already an appointment in that time slot."))
            .build();
      }
    }

    // Update appointment fields
    //    appointment.setUser(parent); //Remain the same
    //    appointment.setTeacher(teacherOpt.get()); //Remain the same
    appointment.setDatetimeStart(request.getDatetimeStart());
    appointment.setDatetimeEnd(request.getDatetimeEnd());

    session.getTransaction().commit();
    session.close();

    return Response.ok().build();
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
        dbi.getListSelectFromWhereEqual(Payment.class, Payment_.placedBy, parent, session);
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
      @PathParam("id") String parentId,
      PostParentPaymentRequest request,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent who made the request
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity(new StatusResponse(Status.NOT_FOUND)).build();
    }
    if (!parent.getEmail().equals(request.getPlacedByEmail())) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT).entity(new StatusResponse(Status.CONFLICT)).build();
    }

    // Fetch the admin entity by email
    Optional<User> adminOpt =
        DatabaseHandler.fetchEntityBy(
            User.class, User_.email, request.getAssignedToEmail(), session);
    if (!adminOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown teacher name"))
          .build();
    }

    // Build the Payment entity
    Payment payment = new Payment();
    payment.setPlacedBy(parent);
    payment.setAssignedTo(adminOpt.get());
    payment.setAmount(request.getAmount());
    payment.setDatetimeDeadline(request.getDatetimeDeadline());
    payment.setDatetimeRequested(request.getDatetimeRequested());
    payment.setDescription(request.getDescription());
    payment.setSubject(request.getSubject());
    payment.setDone(false);
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
      @PathParam("payment_id") String paymentId,
      @PathParam("id") String parentId,
      @Context UriInfo uriInfo) {

    // Fetch appointment
    Optional<Payment> paymentOpt =
        DatabaseHandler.fetchEntityBy(Payment.class, Payment_.id, Integer.parseInt(paymentId));
    if (!paymentOpt.isPresent()) {
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown payment id"))
          .build();
    }

    if (paymentOpt.get().getPlacedBy().getId() != Integer.parseInt(parentId)) {
      return Response.status(Status.CONFLICT)
          .entity(new StatusResponse(Status.CONFLICT, "Not current parent's payment"))
          .build();
    }

    return Response.ok(paymentOpt.get(), MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/payments/{payment_id: [0-9]+}/pay")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @ParentSecured
  @SameParentSecured
  public Response postParentPaymentPaid(
      @PathParam("payment_id") String paymentId,
      @PathParam("id") String parentId,
      PostParentPaymentPayRequest request,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent who made the request
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity(new StatusResponse(Status.NOT_FOUND)).build();
    }
    // Get payment
    Payment payment = session.get(Payment.class, Integer.parseInt(paymentId));
    if (payment == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity(new StatusResponse(Status.NOT_FOUND)).build();
    }

    if (!parent.getEmail().equals(payment.getPlacedBy().getEmail())) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT).entity(new StatusResponse(Status.CONFLICT)).build();
    }

    if (!request.isPaid()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.CONFLICT).entity(new StatusResponse(Status.CONFLICT)).build();
    }

    payment.setDone(true);
    payment.setDatetimeDone(LocalDateTime.now());
    payment.prepareToPersist();

    session.persist(payment);
    session.getTransaction().commit();
    session.close();

    // Here the payment done is returned
    return Response.ok(payment, MediaType.APPLICATION_JSON_TYPE).build();
  }

  @Path("{id: [0-9]+}/notifications")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentNotifications(
      @PathParam("id") String parentId, @Context UriInfo uriInfo) {
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

    // Fetch notification for the parent
    // Three possible notifications type for the parent:

    // 1:General Parents
    List<NotificationGeneralParents> notificationsGP =
        dbi.getListSelectFrom(NotificationGeneralParents.class);

    // 2:Class Parent
    // Initialized an empty NotificationClassParent list
    List<NotificationClassParent> notificationsCP =
        dbi.getListSelectFromWhereEqual(
            NotificationClassParent.class, NotificationClassParent_.text, "FOO", session);
    for (User child : parent.getChildren()) {
      List<Class> classes = dbi.getListSelectFrom(Class.class);
      for (Class cl : classes) {
        for (User student : cl.getClassStudents()) {
          if (child.getEmail().equals(student.getEmail())) {
            notificationsCP.addAll(
                dbi.getListSelectFromWhereEqual(
                    NotificationClassParent.class,
                    NotificationClassParent_.targetClass,
                    cl,
                    session));
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

    ParentNotificationsResponse response = new ParentNotificationsResponse();
    List<URI> notificationsURIs = new ArrayList<>();

    for (NotificationGeneralParents ngp : notificationsGP) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(ngp.getId())).build();
      notificationsURIs.add(uri);
    }

    for (NotificationClassParent ncp : notificationsCP) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(ncp.getId())).build();
      notificationsURIs.add(uri);
    }

    for (NotificationPersonalParent npp : notificationPP) {
      UriBuilder builder = uriInfo.getAbsolutePathBuilder();
      URI uri = builder.path(String.valueOf(npp.getId())).build();
      notificationsURIs.add(uri);
    }

    response.setNotifications(notificationsURIs);

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
      @PathParam("id") String parentId,
      PostParentNotificationRequest request,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent target of the notification
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity(new StatusResponse(Status.NOT_FOUND)).build();
    }

    // Here the admin can POST only a direct notification to this parent

    // Fetch the admin entity by email
    Optional<User> adminOpt =
        DatabaseHandler.fetchEntityBy(User.class, User_.email, request.getCreatorEmail(), session);
    if (!adminOpt.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown admin name"))
          .build();
    }

    // Build the Notification entity
    NotificationPersonalParent npp = new NotificationPersonalParent();
    npp.setCreator(adminOpt.get());
    npp.setTargetUser(parent);
    npp.setDatetime(request.getDatetime());
    npp.setSubject(request.getSubject());
    npp.setText(request.getText());
    npp.prepareToPersist();

    session.persist(npp);
    session.getTransaction().commit();
    session.close();

    URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(npp.getId())).build();
    return Response.created(uri).entity(new StatusResponse(Status.CREATED)).build();
  }

  @Path("{id: [0-9]+}/notifications/{notification_id: [0-9]+}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ParentAdministratorSecured
  @SameParentSecured
  public Response getParentNotificationById(
      @PathParam("notification_id") String notificationId,
      @PathParam("id") String parentId,
      @Context UriInfo uriInfo) {

    DatabaseHandler dbi = DatabaseHandler.getInstance();
    Session session = dbi.getNewSession();
    session.beginTransaction();

    // Get parent target of the notification
    User parent = session.get(User.class, Integer.parseInt(parentId));
    if (parent == null) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND).entity(new StatusResponse(Status.NOT_FOUND)).build();
    }

    // Fetch notification
    Optional<NotificationGeneralParents> notificationOptGP =
        DatabaseHandler.fetchEntityBy(
            NotificationGeneralParents.class,
            NotificationGeneralParents_.id,
            Integer.parseInt(notificationId),
            session);
    Optional<NotificationClassParent> notificationOptCP =
        DatabaseHandler.fetchEntityBy(
            NotificationClassParent.class,
            NotificationClassParent_.id,
            Integer.parseInt(notificationId),
            session);
    Optional<NotificationPersonalParent> notificationOptPP =
        DatabaseHandler.fetchEntityBy(
            NotificationPersonalParent.class,
            NotificationPersonalParent_.id,
            Integer.parseInt(notificationId),
            session);
    if ((!notificationOptGP.isPresent())
        && (!notificationOptCP.isPresent())
        && (!notificationOptPP.isPresent())) {
      session.getTransaction().commit();
      session.close();
      return Response.status(Status.NOT_FOUND)
          .entity(new StatusResponse(Status.NOT_FOUND, "Unknown notification id"))
          .build();
    }

    if (notificationOptGP.isPresent()) {
      session.getTransaction().commit();
      session.close();
      return Response.ok(notificationOptGP.get(), MediaType.APPLICATION_JSON_TYPE).build();
    }

    if (notificationOptCP.isPresent()) {
      // Check if the parent can read the notification
      NotificationClassParent notificationCP = notificationOptCP.get();
      Class targetClass = notificationCP.getTargetClass();
      List<User> children = parent.getChildren();
      for (User child : children) {
        for (User student : targetClass.getClassStudents()) {
          if (child.getId() == student.getId()) {
            session.getTransaction().commit();
            session.close();
            return Response.ok(notificationOptCP.get(), MediaType.APPLICATION_JSON_TYPE).build();
          }
        }
      }
    }

    if (notificationOptPP.isPresent()) {
      if (notificationOptPP.get().getTargetUser().getId() == (parent.getId())) {
        session.getTransaction().commit();
        session.close();
        return Response.ok(notificationOptPP.get(), MediaType.APPLICATION_JSON_TYPE).build();
      }
    }

    session.getTransaction().commit();
    session.close();
    return Response.status(Status.UNAUTHORIZED)
        .entity(
            new StatusResponse(
                Status.UNAUTHORIZED, "Current parent can't get the requested notification"))
        .build();
  }
}

package com.github.polimi_mt_acg.back2school.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
import com.github.polimi_mt_acg.back2school.model.Appointment;
import com.github.polimi_mt_acg.back2school.model.Payment;
import com.github.polimi_mt_acg.back2school.model.User;
import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.DatabaseSeeder;
import com.github.polimi_mt_acg.back2school.utils.TestCategory;
import com.github.polimi_mt_acg.back2school.utils.rest.HTTPServerManager;
import com.github.polimi_mt_acg.back2school.utils.rest.RestFactory;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ParentResourcePaymentsTest {

  private static HttpServer server;

  @BeforeClass
  public static void setUp() throws Exception {
    // Truncate DB
    // Deploy database scenario
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
            HTTPServerManager.startServer(
                    AuthenticationResource.class, "com.github.polimi_mt_acg.back2school.api.v1.parents",
                    "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
  }

  @AfterClass
  public static void tearDown() throws Exception {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentsFromAdmin() throws JsonProcessingException {
    User parent = buildMarcos(1);
    URI parentURI = doParentPost(1,parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin = get(User.Role.ADMINISTRATOR);

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "payments").buildGet();

    Response response = request.invoke();
    //    System.out.println("HERE 2"+response.toString());

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ParentPaymentsResponse parentPayments = response.readEntity(ParentPaymentsResponse.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentPayments));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentsFromSameParent() throws JsonProcessingException {
    User parent = buildMarcos(2);
    URI parentURI = doParentPost(2,parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments").buildGet();

    Response response = request.invoke();
    //    System.out.println("HERE 2"+response.toString());

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ParentPaymentsResponse parentPayments = response.readEntity(ParentPaymentsResponse.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentPayments));
  }


  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromAdmin() throws JsonProcessingException {
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(8, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin = get(User.Role.ADMINISTRATOR);

    URI paymentURI = postPayment(parent.getEmail(),admin.getEmail(),23,parentID);

    // Now query /parents/{parent_id}/Payment from admin
    Invocation requestCheck =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "payments")
                    .buildGet();

    Response responseCheck = requestCheck.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());

    ParentPaymentsResponse parentPayments = responseCheck.readEntity(ParentPaymentsResponse.class);

    assertTrue(parentPayments.getPayments().size() > 0);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(parentPayments));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromParent() throws JsonProcessingException {
    User parent = buildMarcos(4);
    URI parentURI = doParentPost(3, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin =get(User.Role.ADMINISTRATOR);

    PostParentPaymentRequest postParentPaymentRequest = buildPayment( parent.getEmail(), admin.getEmail(),  50);

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments")
                    .buildPost(Entity.json(postParentPaymentRequest));

    Response response = request.invoke();
    System.out.println("HERE Payment Post Refused: "+response.toString());
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }


  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentByIdFromAdministrator() throws JsonProcessingException {
    User parent = buildMarcos(5);
    URI parentURI = doParentPost(3, parent);
    User admin = get(User.Role.ADMINISTRATOR);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 20;

    URI paymentURI = postPayment(parent.getEmail(),admin.getEmail(),amount,parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Invocation requestGetAppointmentByID =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID,"payments", paymentID).buildGet();

    Response response = requestGetAppointmentByID.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    //    System.out.println("HERE1"+response.getStatus());
    Payment paymentResp = response.readEntity(Payment.class);

    assertTrue(paymentResp.getPlacedBy().equals(parent));
    assertTrue(paymentResp.getAssignedTo().equals(admin));
    assertTrue(paymentResp.getAmount() == amount);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(paymentResp));
  }


  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentByIdFromSameParent() throws JsonProcessingException {
    User parent = buildMarcos(6);
    URI parentURI = doParentPost(7, parent);
    User admin = get(User.Role.ADMINISTRATOR);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 10;

    URI paymentURI = postPayment(parent.getEmail(),admin.getEmail(),amount,parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Invocation requestGetAppointmentByID =
            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID,"payments", paymentID).buildGet();

    Response response = requestGetAppointmentByID.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    //    System.out.println("HERE1"+response.getStatus());
    Payment paymentResp = response.readEntity(Payment.class);

    assertTrue(paymentResp.getPlacedBy().equals(parent));
    assertTrue(paymentResp.getAssignedTo().equals(admin));
    assertTrue(paymentResp.getAmount() == amount);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(paymentResp));
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsPaidFromParent() throws JsonProcessingException {
    User parent = buildMarcos(7);
    URI parentURI = doParentPost(5, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    User admin =get(User.Role.ADMINISTRATOR);

    URI paymentURI = postPayment(parent.getEmail(),admin.getEmail(),23,parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    //Do the payment
    PostParentPaymentPayRequest postParentPaymentPayRequest = new PostParentPaymentPayRequest();
    postParentPaymentPayRequest.setPaid(true);
    Invocation requestToPay =
            RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments", paymentID, "pay")
                    .buildPost(Entity.json(postParentPaymentPayRequest));

    Response responsePaid = requestToPay.invoke();


    assertEquals(Response.Status.OK.getStatusCode(), responsePaid.getStatus());

    Payment paymentDONE = responsePaid.readEntity(Payment.class);

    // Print it
    ObjectMapper mapper = RestFactory.objectMapper();
    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(paymentDONE));
  }

  private User get(User.Role role) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");

    List<User> usersWithRole =
            users.stream().filter(user -> user.getRole() == role).collect(Collectors.toList());
    return usersWithRole.get(0);
  }

  private User buildMarcos(int copyNumber) {
    User marcos = new User();
    marcos.setName("Marcos " + copyNumber);
    marcos.setSurname("Ferdinand " + copyNumber);
    marcos.setEmail("marcos.ferdinand" + copyNumber + "@mail.com");
    marcos.setSeedPassword("marcos_password");
    marcos.setRole(User.Role.PARENT);
    marcos.prepareToPersist();
    return marcos;
  }

  private User getAChild(int copynumber) {
    List<User> users =
            (List<User>) DatabaseSeeder.getEntitiesListFromSeed("scenarioParents", "users.json");
    List<User> children =
            users
                    .stream()
                    .filter(user -> user.getRole().equals(User.Role.STUDENT))
                    .collect(Collectors.toList());
    return children.get(copynumber);
  }

  /**
   * Do a post an return the inserted parent URI.
   *
   * @return The inserted resource URI.
   */
  private URI doParentPost(int copynumber, User parent) {
    User child = getAChild(copynumber);
    String childEmail = child.getEmail();

    // Now build a PostParentRequest
    PostParentRequest request = new PostParentRequest();
    request.setParentAndPassword(parent, parent.getSeedPassword());
    request.setStudentEmail(childEmail);

    User admin = get(User.Role.ADMINISTRATOR);

    // Make a POST to /parents
    Invocation post =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents")
                    .buildPost(Entity.json(request));

    Response response = post.invoke();
    System.out.println("HERE POST REQ" + response.toString());

    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }

  private boolean weakEquals(User u, User p) {
    return u.getName().equals(p.getName())
            && u.getSurname().equals(p.getSurname())
            && u.getEmail().equals(p.getEmail());
  }

  private PostParentPaymentRequest buildPayment(String placedByEmail, String assignedToEmail, double amount){
    PostParentPaymentRequest payment = new PostParentPaymentRequest();
    payment.setPlacedByEmail(placedByEmail);
    payment.setAssignedToEmail(assignedToEmail);
    payment.setAmount(amount);
    payment.setSubject("New english book");
    payment.setDescription("The new english book sold directly by the school");
    payment.setDatetimeRequested(LocalDateTime.now());
    payment.setDatetimeDeadline(LocalDateTime.now().plusDays(7));
    return payment;
  }


  private URI postPayment( String placedByEmail, String assignedToEmail, double amount, String parentID){
    User admin = get(User.Role.ADMINISTRATOR);
    //We post a payment between parent and admin
    PostParentPaymentRequest postParentPaymentRequest = buildPayment( placedByEmail,  assignedToEmail,  amount);

    Invocation request =
            RestFactory.getAuthenticatedInvocationBuilder(admin, "parents", parentID, "payments")
                    .buildPost(Entity.json(postParentPaymentRequest));

    Response response = request.invoke();
    System.out.println("HERE Payment Post: "+response.toString());
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

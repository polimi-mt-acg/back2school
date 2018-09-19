package com.github.polimi_mt_acg.back2school.api.v1;

import com.github.polimi_mt_acg.back2school.api.v1.auth.AuthenticationResource;
import com.github.polimi_mt_acg.back2school.api.v1.parents.*;
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

import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.buildMarcos;
import static com.github.polimi_mt_acg.back2school.api.v1.ParentsResourceTest.doParentPost;
import static com.github.polimi_mt_acg.back2school.utils.PythonMockedUtilityFunctions.print;
import static org.junit.Assert.*;

public class ParentsResourcePaymentsTest {

  private static HttpServer server;
  private static User adminForAuth;

  @BeforeClass
  public static void setUp() {
    DatabaseSeeder.deployScenario("scenarioParents");

    // Run HTTP server
    server =
        HTTPServerManager.startServer(
            AuthenticationResource.class,
            "com.github.polimi_mt_acg.back2school.api.v1.parents",
            "com.github.polimi_mt_acg.back2school.api.v1.security_contexts");
    // load admin for authentication
    adminForAuth = DatabaseSeeder.getSeedUserByRole("scenarioParents", User.Role.ADMINISTRATOR);
  }

  @AfterClass
  public static void tearDown() {
    // Truncate DB
    DatabaseHandler.getInstance().truncateDatabase();
    DatabaseHandler.getInstance().destroy();

    // Close HTTP server
    server.shutdownNow();
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentsFromAdmin() {
    User parent = buildMarcos(1);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentID, "payments")
            .buildGet();

    Response response = request.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ParentPaymentsResponse parentPayments = response.readEntity(ParentPaymentsResponse.class);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentsFromSameParent() {
    User parent = buildMarcos(2);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments")
            .buildGet();

    Response response = request.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    ParentPaymentsResponse parentPayments = response.readEntity(ParentPaymentsResponse.class);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromAdmin() {
    User parent = buildMarcos(3);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    postPayment(parent.getEmail(), adminForAuth.getEmail(), 23, parentID);

    // Now query /parents/{parent_id}/Payment from admin
    Invocation requestCheck =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentID, "payments")
            .buildGet();

    Response responseCheck = requestCheck.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), responseCheck.getStatus());

    ParentPaymentsResponse parentPayments = responseCheck.readEntity(ParentPaymentsResponse.class);

    assertTrue(parentPayments.getPayments().size() > 0);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromParent() {
    User parent = buildMarcos(4);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    PostParentPaymentRequest postParentPaymentRequest =
        buildPayment(parent.getEmail(), adminForAuth.getEmail(), 50);

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments")
            .buildPost(Entity.json(postParentPaymentRequest));

    Response response = request.invoke();
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    print("Payment Post Refused: ", response.toString());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentByIdFromAdministrator() {
    User parent = buildMarcos(5);
    URI parentURI = doParentPost(adminForAuth, parent);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 20;

    URI paymentURI = postPayment(parent.getEmail(), adminForAuth.getEmail(), amount, parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Invocation requestGetAppointmentByID =
        RestFactory.getAuthenticatedInvocationBuilder(
                adminForAuth, "parents", parentID, "payments", paymentID)
            .buildGet();

    Response response = requestGetAppointmentByID.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Payment paymentResp = response.readEntity(Payment.class);

    assertEquals(paymentResp.getPlacedBy(), parent);
    assertEquals(paymentResp.getAssignedTo(), adminForAuth);
    assertEquals(paymentResp.getAmount(), amount, 0.0);

    // Print it
    print(paymentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentByIdFromSameParent() {
    User parent = buildMarcos(6);
    URI parentURI = doParentPost(adminForAuth, parent);

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 10;

    URI paymentURI = postPayment(parent.getEmail(), adminForAuth.getEmail(), amount, parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Invocation requestGetAppointmentByID =
        RestFactory.getAuthenticatedInvocationBuilder(
                parent, "parents", parentID, "payments", paymentID)
            .buildGet();

    Response response = requestGetAppointmentByID.invoke();
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Payment paymentResp = response.readEntity(Payment.class);

    assertEquals(paymentResp.getPlacedBy(), parent);
    assertEquals(paymentResp.getAssignedTo(), adminForAuth);
    assertEquals(paymentResp.getAmount(), amount, 0.0);

    // Print it
    print(paymentResp);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsPaidFromParent() {
    User parent = buildMarcos(7);
    URI parentURI = doParentPost(adminForAuth, parent);

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    URI paymentURI = postPayment(parent.getEmail(), adminForAuth.getEmail(), 23, parentID);

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    // Do the payment
    PostParentPaymentPayRequest postParentPaymentPayRequest = new PostParentPaymentPayRequest();
    postParentPaymentPayRequest.setPaid(true);
    Invocation requestToPay =
        RestFactory.getAuthenticatedInvocationBuilder(
                parent, "parents", parentID, "payments", paymentID, "pay")
            .buildPost(Entity.json(postParentPaymentPayRequest));

    Response responsePaid = requestToPay.invoke();

    assertEquals(Response.Status.OK.getStatusCode(), responsePaid.getStatus());

    Payment paymentDONE = responsePaid.readEntity(Payment.class);

    // Print it
    print(paymentDONE);
  }

  private PostParentPaymentRequest buildPayment(
      String placedByEmail, String assignedToEmail, double amount) {
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

  private URI postPayment(
      String placedByEmail, String assignedToEmail, double amount, String parentID) {

    // We post a payment between parent and admin
    PostParentPaymentRequest postParentPaymentRequest =
        buildPayment(placedByEmail, assignedToEmail, amount);

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(adminForAuth, "parents", parentID, "payments")
            .buildPost(Entity.json(postParentPaymentRequest));

    Response response = request.invoke();
    System.out.println("HERE Payment Post: " + response.toString());
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

    URI resourceURI = response.getLocation();
    assertNotNull(resourceURI);
    return resourceURI;
  }
}

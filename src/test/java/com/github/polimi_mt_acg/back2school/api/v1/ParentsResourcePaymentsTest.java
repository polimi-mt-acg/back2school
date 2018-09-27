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
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentPaymentsResponse parentPayments =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "payments")
            .readEntity(ParentPaymentsResponse.class);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentsFromSameParent() {
    User parent = buildMarcos(2);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentPaymentsResponse parentPayments =
        RestFactory.doGetRequest(parent, "parents", parentID, "payments")
            .readEntity(ParentPaymentsResponse.class);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromAdmin() {
    User parent = buildMarcos(3);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentPaymentRequest parentPaymentRequest = buildPayment(Payment.Type.TRIP, 23);
    RestFactory.doPostRequest(adminForAuth, parentPaymentRequest, "parents", parentID, "payments");

    // Now query /parents/{parent_id}/Payment from admin
    ParentPaymentsResponse parentPayments =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "payments")
            .readEntity(ParentPaymentsResponse.class);

    assertTrue(parentPayments.getPayments().size() > 0);

    // Print it
    print(parentPayments);
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void postParentPaymentsFromParent() {
    User parent = buildMarcos(4);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentPaymentRequest parentPaymentRequest = buildPayment(Payment.Type.MATERIAL, 50);

    Invocation request =
        RestFactory.getAuthenticatedInvocationBuilder(parent, "parents", parentID, "payments")
            .buildPost(Entity.json(parentPaymentRequest));

    Response response = request.invoke();
    assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    print("Payment Post Refused: ", response.toString());
  }

  @Test
  @Category(TestCategory.Endpoint.class)
  public void getParentPaymentByIdFromAdministrator() {
    User parent = buildMarcos(5);
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 20;

    ParentPaymentRequest parentPaymentRequest = buildPayment(Payment.Type.TRIP, amount);
    URI paymentURI =
        RestFactory.doPostRequest(
            adminForAuth, parentPaymentRequest, "parents", parentID, "payments");

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Payment paymentResp =
        RestFactory.doGetRequest(adminForAuth, "parents", parentID, "payments", paymentID)
            .readEntity(Payment.class);

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
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    // Now query /parents/{bob_id} from admin
    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    int amount = 10;
    ParentPaymentRequest parentPaymentRequest = buildPayment(Payment.Type.MATERIAL, 10);
    URI paymentURI =
        RestFactory.doPostRequest(
            adminForAuth, parentPaymentRequest, "parents", parentID, "payments");

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    Payment paymentResp =
        RestFactory.doGetRequest(parent, "parents", parentID, "payments", paymentID)
            .readEntity(Payment.class);

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
    URI parentURI = RestFactory.doPostRequest(adminForAuth, parent, "parents");

    Path fullPath = Paths.get("/", parentURI.getPath());
    Path idPath = fullPath.getParent().relativize(fullPath);
    String parentID = idPath.toString();

    ParentPaymentRequest parentPaymentRequest = buildPayment(Payment.Type.MATERIAL, 23);
    URI paymentURI =
        RestFactory.doPostRequest(
            adminForAuth, parentPaymentRequest, "parents", parentID, "payments");

    Path fullPayPath = Paths.get("/", paymentURI.getPath());
    Path idPayPath = fullPayPath.getParent().relativize(fullPayPath);
    String paymentID = idPayPath.toString();

    // Do the payment
    Response response =
        RestFactory.getAuthenticatedInvocationBuilder(
                parent, "parents", parentID, "payments", paymentID, "pay")
            .buildPost(Entity.json("{}"))
            .invoke();

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


    // Print it
    print("POST /parents/", parentID, "/payments/", paymentID, "/pay");
  }

  private ParentPaymentRequest buildPayment(Payment.Type type, double amount) {
    ParentPaymentRequest paymentRequest = new ParentPaymentRequest();
    paymentRequest.setType(type);
    paymentRequest.setDatetimeRequested(LocalDateTime.now());
    // paymentRequest.setDatetimeDone(null);
    paymentRequest.setDatetimeDeadline(LocalDateTime.now().plusDays(7));
    // paymentRequest.setDone(false);
    paymentRequest.setSubject("New english book");
    paymentRequest.setDescription("The new english book sold directly by the school");
    paymentRequest.setAmount(amount);
    return paymentRequest;
  }
}

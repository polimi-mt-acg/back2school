package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;
import com.github.polimi_mt_acg.back2school.utils.RandomStringGenerator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import org.hibernate.Session;

@Entity
@Table(name = "authentication_session")
public class AuthenticationSession implements DeserializeToPersistInterface {

  /**
   * The default session duration time. It's the interval period between the last user interaction
   * and next future interactions the user is allowed to do without being force to do the login
   * again
   */
  private static final Duration SESSION_DURATION = Duration.parse("PT30M"); // 30 minutes

  @Transient public String seedUserEmail;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @ManyToOne
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "SESSION_USER_ID_FK"))
  private User user;

  @Column(name = "token")
  private String token = RandomStringGenerator.generateString();

  @Column(name = "datetime_last_interaction")
  private LocalDateTime datetimeLastInteraction = LocalDateTime.now();

  @Column(name = "cancelled")
  private boolean cancelled = false;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public LocalDateTime getDatetimeLastInteraction() {
    return datetimeLastInteraction;
  }

  public void setDatetimeLastInteraction(LocalDateTime datetimeLastInteraction) {
    this.datetimeLastInteraction = datetimeLastInteraction;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public void prepareToPersist() {
    seedAssociateUser();
  }

  private void seedAssociateUser() {
    if (seedUserEmail != null) {
      DatabaseHandler dhi = DatabaseHandler.getInstance();
      List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedUserEmail);
      if (users != null) {
        setUser(users.get(0));
      }
    }
  }

  /**
   * Get the current authenticated user, if any, according to AuthenticationSession validity.
   *
   * <p>Overall procedure: 1. Get the token from the header. 2. Use the token to retrieve the linked
   * AuthenticationSession. 3. Check if there exist an AuthenticationSession corresponding to the
   * token. 4. Check if the AuthenticationSession is still valid (not cancelled). 5. Check if the
   * duration of the user session is not gone over (w.r.t. the last interaction) 6. Update the
   * last_interaction_datetime of the session. 7. Return the user associated to the
   * AuthenticationSession of the token.
   *
   * @param requestContext The current request context.
   * @return
   */
  public static User getCurrentUser(ContainerRequestContext requestContext) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    User currentUser = getCurrentUser(requestContext, session);

    session.getTransaction().commit();
    session.close();
    return currentUser;
  }

  public static User getCurrentUser(ContainerRequestContext requestContext, Session session) {
    if (requestContext == null) return null;
    String bearerAuthorizationToken = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
    return getUserFromToken(bearerAuthorizationToken, session);
  }

  /**
   * Get the current authenticated user, if any, according to AuthenticationSession validity.
   *
   * <p>Overall procedure: 1. Get the token from the header. 2. Use the token to retrieve the linked
   * AuthenticationSession. 3. Check if there exist an AuthenticationSession corresponding to the
   * token. 4. Check if the AuthenticationSession is still valid (not cancelled). 5. Check if the
   * duration of the user session is not gone over (w.r.t. the last interaction) 6. Update the
   * last_interaction_datetime of the session. 7. Return the user associated to the
   * AuthenticationSession of the token.
   *
   * @param HTTPHeaders The current HTTPHeaders.
   * @return
   */
  public static User getCurrentUser(HttpHeaders HTTPHeaders) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    User currentUser = getCurrentUser(HTTPHeaders, session);

    session.getTransaction().commit();
    session.close();
    return currentUser;
  }

  public static User getCurrentUser(HttpHeaders HTTPHeaders, Session session) {
    if (HTTPHeaders == null) return null;
    String authorizationToken = HTTPHeaders.getHeaderString(HttpHeaders.AUTHORIZATION);
    return getUserFromToken(authorizationToken, session);
  }

  /**
   * Get the current authenticated user, if any, according to the given token.
   *
   * @param bearerAuthorizationToken The token with prefix "Bearer " corresponding to the session.
   * @param session The Hibernate session to use.
   * @return
   */
  public static User getUserFromToken(String bearerAuthorizationToken, Session session) {
    // Validate the Authorization token
    if (bearerAuthorizationToken == null) return null;
    String token = bearerAuthorizationToken.replace("Bearer ", "");
    token = token.trim();
    if (token.isEmpty()) return null;

    // query to get the last valid AuthenticationSession corresponding to the token
    // ------------------------------------
    CriteriaBuilder builder = session.getCriteriaBuilder();

    CriteriaQuery<AuthenticationSession> criteria =
        builder.createQuery(AuthenticationSession.class);
    Root<AuthenticationSession> root = criteria.from(AuthenticationSession.class);
    criteria.select(root);
    criteria.where(builder.equal(root.get(AuthenticationSession_.token), token));

    List<AuthenticationSession> authSessions = session.createQuery(criteria).getResultList();
    // ------------------------------------

    // no session found?
    if (authSessions.size() == 0) return null;

    AuthenticationSession authSession = authSessions.get(0);

    // Have been the session cancelled by a logout or some other reason?
    if (authSession.isCancelled()) return null;

    // is the session still valid with respect to the time duration constraint?
    LocalDateTime sessionDatetimeValidityOffset = authSession.getDatetimeLastInteraction();

    sessionDatetimeValidityOffset = sessionDatetimeValidityOffset.plus(SESSION_DURATION);

    // if the time validity offset is over the current time: session is not more valid
    if (sessionDatetimeValidityOffset.isBefore(LocalDateTime.now())) return null;

    /* ****  Otherwise, just update the last interaction attribute **** */
    authSession.setDatetimeLastInteraction(LocalDateTime.now());
    session.save(authSession);

    return authSession.getUser();
  }

  /** Provide a new AuthenticationSession invalidating all the previously eventually active ones. */
  public static AuthenticationSession startNewAuthenticationSession(User user) {
    Session session = DatabaseHandler.getInstance().getNewSession();
    session.beginTransaction();

    // just start with a new fresh session: get invalid all the other sessions
    invalidateAllAuthenticationSession(user, session);

    // create a new AuthenticationSession and associate it to the user
    AuthenticationSession authSession = new AuthenticationSession();
    authSession.setUser(user);

    // persist the new session
    session.persist(authSession);

    session.getTransaction().commit();
    session.close();

    return authSession;
  }

  /**
   * Invalidate all the still valid (set AuthenticationSession.cancelled = true) sessions associated
   * to the user.
   *
   * @param user The user to which invalidate the sessions
   * @param session The hibernate session
   */
  public static void invalidateAllAuthenticationSession(User user, Session session) {
    // get all the previous still valid authentication sessions
    List<AuthenticationSession> sessionsStillValid = getValidAuthenticationSession(user, session);

    // proceed to invalidate all the still valid sessions
    for (AuthenticationSession authSession : sessionsStillValid) {
      // invalidate the authSession and save it
      authSession.setCancelled(true);

      session.persist(authSession);
    }
  }

  /**
   * Get the list of still valid AuthenticationSession associated to an user. By still valid it is
   * meant: AuthenticationSession.cancelled = false
   *
   * @param user The user to which retrieve active sessions
   * @param session The hibernate session
   * @return
   */
  private static List<AuthenticationSession> getValidAuthenticationSession(
      User user, Session session) {
    // Define the CriteriaQuery
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<AuthenticationSession> cq = cb.createQuery(AuthenticationSession.class);
    Root<AuthenticationSession> root = cq.from(AuthenticationSession.class);
    cq.where(
        cb.and(
            cb.equal(
                root.get(AuthenticationSession_.user), user), // sessions associated to this user
            cb.equal(root.get(AuthenticationSession_.cancelled), false) // still valid sessions
            ));
    // order DESC by the datetimeLastInteraction in order to get first the
    // most recent session that are still valid
    cq.orderBy(cb.desc(root.get(AuthenticationSession_.datetimeLastInteraction)));

    return session.createQuery(cq).getResultList();
  }
}

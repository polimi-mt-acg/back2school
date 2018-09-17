package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.utils.RandomStringGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "user")
@XmlRootElement
public class User implements DeserializeToPersistInterface {

  private static final Logger LOGGER = Logger.getLogger(User.class.getName());

  @Transient
  private String newPassword;

  @Id
  @GeneratedValue
  @Column(name = "id")
  private int id;

  @Enumerated(EnumType.STRING)
  @Column(name = "role")
  private Role role = Role.STUDENT;

  @Column(name = "name")
  private String name;

  @Column(name = "surname")
  private String surname;

  @Column(name = "email")
  private String email;

  @Column(name = "password")
  private String password;

  @Column(name = "salt")
  private String salt;

  @ManyToMany
  @JoinTable(
    name = "user_notification_read",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "notification_id")
  )
  private List<Notification> notificationsRead = new ArrayList<>();

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "parent_children",
      joinColumns = @JoinColumn(name = "parent_id"),
      inverseJoinColumns = @JoinColumn(name = "child_id"))
  private List<User> children = new ArrayList<>();

  @Override
  public void prepareToPersist() {
    if (newPassword != null) {
      setPassword(this.newPassword);
    }
  }

  @JsonIgnore
  public int getId() {
    return id;
  }

  @JsonProperty
  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @JsonIgnore
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = getStringHash(password);
  }

  @JsonIgnore
  public String getSalt() {
    return salt;
  }

  @JsonProperty
  public void setSalt(String salt) {
    this.salt = salt;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  private String getStringHash(String string) {
    if (this.salt == null) {
      this.salt = RandomStringGenerator.generateString();
    }

    KeySpec spec = new PBEKeySpec(string.toCharArray(), this.salt.getBytes(), 65536, 128);

    byte[] hash = new byte[0];
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      hash = f.generateSecret(spec).getEncoded();
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    Base64.Encoder enc = Base64.getEncoder();
    return enc.encodeToString(hash);
  }

  /**
   * Check the given password validity.
   *
   * @param passwordToCheck the password to check
   */
  public boolean passwordEqualsTo(String passwordToCheck) {
    if (this.salt == null || this.password == null) {
      return false;
    }

    String hashOfPasswordToCheck = getStringHash(passwordToCheck);
    return this.password.equals(hashOfPasswordToCheck);
  }

  @JsonIgnore
  public String getNewPassword() {
    return newPassword;
  }

  @JsonProperty("new_password")
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  @JsonIgnore
  public List<Notification> getNotificationsRead() {
    return notificationsRead;
  }

  public void addNotificationsRead(Notification notification) {
    this.notificationsRead.add(notification);
  }

  public void setNotificationsRead(List<Notification> notificationsRead) {
    this.notificationsRead = notificationsRead;
  }

  @JsonIgnore
  public List<User> getChildren() {
    return children;
  }

  public void addChild(User student) {
    children.add(student);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    return id == user.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  public enum Role {
    STUDENT,
    PARENT,
    TEACHER,
    ADMINISTRATOR
  }

  /**
   * Test weak equality against another object. Attributes tested to be equal: name, surname, email,
   * role.
   *
   * @param obj The object to be compared.
   * @return true if weak equality holds.
   */
  public boolean weakEquals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!User.class.isAssignableFrom(obj.getClass())) {
      return false;
    }
    final User other = (User) obj;

    // skip id

    // name
    if ((this.getName() == null)
        ? (other.getName() != null)
        : !this.getName().equals(other.getName())) return false;

    // surname
    if ((this.getSurname() == null)
        ? (other.getSurname() != null)
        : !this.getSurname().equals(other.getSurname())) return false;

    // email
    if ((this.getEmail() == null)
        ? (other.getEmail() != null)
        : !this.getEmail().equals(other.getEmail())) return false;

    // role
    if ((this.getRole() == null)
        ? (other.getRole() != null)
        : !this.getRole().equals(other.getRole())) return false;

    return true;
  }
}

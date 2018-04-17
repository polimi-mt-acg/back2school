package com.github.polimi_mt_acg.back2school.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.polimi_mt_acg.back2school.utils.RandomStringGenerator;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

@Entity
@Table(name = "user")
@XmlRootElement
public class User implements DeserializeToPersistInterface {

    private final static Logger LOGGER =
            Logger.getLogger(User.class.getName());
    @Transient
    private String seedPassword;
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
            joinColumns = @JoinColumn(name = "notification_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<Notification> notificationsRead = new ArrayList<>();

    @Override
    public void prepareToPersist() {
        if (seedPassword != null) {
            setPassword(this.seedPassword);
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
            // generate salt from a new UUID
            this.salt = RandomStringGenerator.generateString();
        }

        KeySpec spec =
                new PBEKeySpec(string.toCharArray(), this.salt.getBytes(), 65536, 128);

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
     * @return
     */
    public boolean passwordEqualsTo(String passwordToCheck) {
        if (this.salt == null || this.password == null) return false;

        String hashOfPasswordToCheck = getStringHash(passwordToCheck);
        return this.password.equals(hashOfPasswordToCheck);
    }

    @JsonIgnore
    public String getSeedPassword() {
        return seedPassword;
    }

    @JsonProperty
    public void setSeedPassword(String seedPassword) {
        this.seedPassword = seedPassword;
    }

    @JsonIgnore
    public List<Notification> getNotificationsRead() {
        return notificationsRead;
    }

    public void addNotificationsRead(Notification notification) {
        this.notificationsRead.add(notification);
    }

    public enum Role {
        STUDENT, PARENT, TEACHER, ADMINISTRATOR
    }
}

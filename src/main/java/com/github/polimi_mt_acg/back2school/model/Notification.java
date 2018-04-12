package com.github.polimi_mt_acg.back2school.model;

import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = javax.persistence.DiscriminatorType.STRING)
public class Notification implements DeserializeToPersistInterface {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "creator_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
    private User creator;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @Column(name = "subject")
    private String subject;

    @Column(name = "text")
    private String text;

    @Transient
    public String seedCreatorEmail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public LocalDateTime getDatetime() {
        return datetime;
    }

    public void setDatetime(LocalDateTime datetime) {
        this.datetime = datetime;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void prepareToPersist() {
        seedAssociateCreator();
    }

    private void seedAssociateCreator() {
        if (seedCreatorEmail != null) {
            DatabaseHandler dhi = DatabaseHandler.getInstance();
            List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedCreatorEmail);
            if (users != null) {
                setCreator(users.get(0));
            }
        }
    }
}

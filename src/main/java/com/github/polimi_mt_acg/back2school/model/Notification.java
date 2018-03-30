package com.github.polimi_mt_acg.back2school.model;


import org.hibernate.type.DiscriminatorType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn( name = "type", discriminatorType = javax.persistence.DiscriminatorType.STRING)
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "creator_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
    private User creator;

    @Column(name = "date")
    private LocalDateTime datetime;

    @Column(name = "subject")
    private String subject;

    @Column(name = "text")
    private String text;

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
}

package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "creator_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
    private User creator;

    @ManyToOne
    @JoinColumn(name = "target_user_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_TARGET_USER_ID_FK"))
    private User targetUser;

    @ManyToOne
    @JoinColumn(name = "target_class_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_TARGET_CLASS_ID_FK"))
    private Class targetClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type = Type.GENERAL;

    @Column(name = "date")
    private LocalDateTime datatime;

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

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getDatatime() {
        return datatime;
    }

    public void setDatatime(LocalDateTime datatime) {
        this.datatime = datatime;
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

    public enum Type {
        GENERAL, CLASS, DIRECT
    }
}




package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public class Notification {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "creator_id",
            foreignKey = @ForeignKey(name = "NOTIFICATION_CREATOR_ID_FK"))
    private int creatorId;

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

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
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

}




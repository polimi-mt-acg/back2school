package com.github.polimi_mt_acg.back2school.model;


import com.github.polimi_mt_acg.back2school.utils.DatabaseHandler;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment")
public class Payment implements DeserializeToPersistInterface {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "placed_by_id",
            foreignKey = @ForeignKey(name = "PLACED_BY_USER_ID_FK")
    )
    private User placedBy;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id",
            foreignKey = @ForeignKey(name = "ASSIGNED_TO_USER_ID_FK")
    )
    private User assignedTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type = Type.MATERIAL;

    @Column(name = "datetime_requested")
    private LocalDateTime datetimeRequested;

    @Column(name = "datetime_done")
    private LocalDateTime datetimeDone;

    @Column(name = "datetime_deadline")
    private LocalDateTime datetimeDeadline;

    @Column(name = "done")
    private boolean done;

    @Column(name = "subject")
    private String subject;

    @Column(name = "description")
    private String description;

    @Column(name = "amount")
    private double amount;

    @Transient
    public String seedPlacedByUserEmail;

    @Transient
    public String seedAssignedToUserEmail;

    @Override
    public void prepareToPersist() {
        seedAssociatePlacedBy();
        seedAssociateAssignedTo();
    }

    public enum Type {
        MATERIAL, MONTHLY, TRIP
    }

    private void seedAssociatePlacedBy() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedPlacedByUserEmail);
        if (users != null) {
            setPlacedBy(users.get(0));
        }
    }

    private void seedAssociateAssignedTo() {
        DatabaseHandler dhi = DatabaseHandler.getInstance();
        List<User> users = dhi.getListSelectFromWhereEqual(User.class, User_.email, seedAssignedToUserEmail);
        if (users != null) {
            setAssignedTo(users.get(0));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getPlacedBy() {
        return placedBy;
    }

    public void setPlacedBy(User placedBy) {
        this.placedBy = placedBy;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public LocalDateTime getDatetimeRequested() {
        return datetimeRequested;
    }

    public void setDatetimeRequested(LocalDateTime datetimeRequested) {
        this.datetimeRequested = datetimeRequested;
    }

    public LocalDateTime getDatetimeDone() {
        return datetimeDone;
    }

    public void setDatetimeDone(LocalDateTime datetimeDone) {
        this.datetimeDone = datetimeDone;
    }

    public LocalDateTime getDatetimeDeadline() {
        return datetimeDeadline;
    }

    public void setDatetimeDeadline(LocalDateTime datetimeDeadline) {
        this.datetimeDeadline = datetimeDeadline;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

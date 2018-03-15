package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", unique = true)
    private RoleName role = RoleName.STUDENT;

    // TODO CRUD permissions to be managed..


    public enum RoleName {
        STUDENT, PARENT, TEACHER, ADMINISTRATOR
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RoleName getRole() {
        return role;
    }

    public void setRole(RoleName role) {
        this.role = role;
    }
}

package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;
import javax.persistence.Table;

@Entity
@Table(name = "class")
public class Class implements DeserializeToPersistInterface {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "academic_year")
    private int accademicYear;

    @Column(name = "name")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAccademicYear() {
        return accademicYear;
    }

    public void setAccademicYear(int accademicYear) {
        this.accademicYear = accademicYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void prepareToPersist() {

    }
}

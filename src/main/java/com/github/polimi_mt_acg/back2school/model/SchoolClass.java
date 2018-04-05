package com.github.polimi_mt_acg.back2school.model;


import javax.persistence.*;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "school_class")
public class SchoolClass implements DeserializeToPersistInterface {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name = "academic_year")
    private int academicYear;

    @Column(name = "name")
    private String name;


    @ManyToMany
    @JoinTable(
           name = "user_school_class",
           joinColumns = @JoinColumn(name = "user_id"),
           inverseJoinColumns = @JoinColumn(name = "school_class_id"))
    private List<User> studentsOfTheClass;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(int academicYear) {
        this.academicYear = academicYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getStudentsOfTheClass() {
        return studentsOfTheClass;
    }

    public void setStudentsOfTheClass(List<User> studentsOfTheClass) {
        this.studentsOfTheClass = studentsOfTheClass;
    }

    @Override
    public void prepareToPersist() {

    }
}

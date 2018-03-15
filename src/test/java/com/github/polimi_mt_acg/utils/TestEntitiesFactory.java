package com.github.polimi_mt_acg.utils;

import com.github.polimi_mt_acg.back2school.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestEntitiesFactory {
    // testStudent data
    private static final String studentName = "testStudentName";
    private static final String studentSurname = "testStudentSurname";
    private static final String studentEmail = "testStudent@email.com";
    private static final String studentPassword = "testStudentPassword";
    private static final String studentSalt = "testStudentSalt";
    private static final User.Type studentType = User.Type.STUDENT;

    // testParent data
    private static final String parentName = "testParentName";
    private static final String parentSurname = "testParentSurname";
    private static final String parentEmail = "testParent@email.com";
    private static final String parentPassword = "testParentPassword";
    private static final String parentSalt = "testParentSalt";
    private static final User.Type parentType = User.Type.PARENT;

    // testTeacher data
    private static final String teacherName = "testTeacherName";
    private static final String teacherSurname = "testTeacherSurname";
    private static final String teacherEmail = "testTeacher@email.com";
    private static final String teacherPassword = "testTeacherPassword";
    private static final String teacherSalt = "testTeacherSalt";
    private static final User.Type teacherType = User.Type.TEACHER;

    // testAdministrator data
    private static final String administratorName = "testAdministratorName";
    private static final String administratorSurname = "testAdministratorSurname";
    private static final String administratorEmail = "testAdministrator@email.com";
    private static final String administratorPassword = "testAdministratorPassword";
    private static final String administratorSalt = "testAdministratorSalt";
    private static final User.Type administratorType = User.Type.ADMINISTRATOR;

    // testSubject data
    private static final String subjectName = "Latin";
    private static final String subjectDescription = "A course on a classical language belonging to the Italic " +
            "branch of the Indo-European languages.";

    // testClassroom data
    private static final String classroomName = "2";
    private static final int classroomFloor = 1;
    private static final String classroomBuilding = "D";

    // testTestGrade data
    private static final double gradeGrade = 7.5;
    private static final String gradeTitle = "Midterm Latin exam";

    // testPayment data
    private static final LocalDateTime paymentDatetimeRequested = LocalDateTime.of(2018, 3, 15, 8, 0);
    private static final LocalDateTime paymentDatetimeDone = LocalDateTime.of(2018, 3, 15, 10, 0);
    private static final LocalDateTime paymentDatetimeDeadline = LocalDateTime.of(2018, 3, 15, 12, 0);
    private static final boolean paymentDone = true;
    private static final String paymentSubject = "School trip to Berlin";
    private static final String paymentDescription = "Fees and costs for the trip";
    private static final double paymentAmount = 450.42;

    public static User buildStudent() {
        User testStudent = new User();
        testStudent.setType(studentType);
        testStudent.setName(studentName);
        testStudent.setSurname(studentSurname);
        testStudent.setEmail(studentEmail);
        testStudent.setPassword(studentPassword);
        testStudent.setSalt(studentSalt);

        return testStudent;
    }

    public static User buildParent() {
        User testEntity = new User();
        testEntity.setType(parentType);
        testEntity.setName(parentName);
        testEntity.setSurname(parentSurname);
        testEntity.setEmail(parentEmail);
        testEntity.setPassword(parentPassword);
        testEntity.setSalt(parentSalt);

        return testEntity;
    }

    public static User buildTeacher() {
        User testTeacher = new User();
        testTeacher.setType(teacherType);
        testTeacher.setName(teacherName);
        testTeacher.setSurname(teacherSurname);
        testTeacher.setEmail(teacherEmail);
        testTeacher.setPassword(teacherPassword);
        testTeacher.setSalt(teacherSalt);

        return testTeacher;
    }

    public static User buildAdministrator() {
        User testEntity = new User();
        testEntity.setType(administratorType);
        testEntity.setName(administratorName);
        testEntity.setSurname(administratorSurname);
        testEntity.setEmail(administratorEmail);
        testEntity.setPassword(administratorPassword);
        testEntity.setSalt(administratorSalt);

        return testEntity;
    }

    public static Subject buildSubject() {
        Subject testSubject = new Subject();
        testSubject.setName(subjectName);
        testSubject.setDescription(subjectDescription);

        return testSubject;
    }

    public static Classroom buildClassroom() {
        Classroom testClassroom = new Classroom();
        testClassroom.setBuilding(classroomBuilding);
        testClassroom.setFloor(classroomFloor);
        testClassroom.setName(classroomName);

        return testClassroom;
    }

    public static Lecture buildLecture() {
        Lecture testLecture = new Lecture();
        testLecture.setDatetimeStart(LocalDateTime.of(2018, 03, 15, 8, 0));
        testLecture.setDatetimeEnd(LocalDateTime.of(2018, 03, 15, 10, 0));

        return testLecture;
    }

    public static Grade buildGrade() {
        Grade testGrade = new Grade();
        testGrade.setDate(LocalDate.now());
        testGrade.setGrade(gradeGrade);
        testGrade.setTitle(gradeTitle);

        return testGrade;
    }

    public static Payment buildPayment(Payment.Type type) {
        Payment testEntity = new Payment();
        testEntity.setType(type);
        testEntity.setDatetimeRequested(paymentDatetimeRequested);
        testEntity.setDatetimeDone(paymentDatetimeDone);
        testEntity.setDatetimeDeadline(paymentDatetimeDeadline);
        testEntity.setDone(paymentDone);
        testEntity.setSubject(paymentSubject);
        testEntity.setDescription(paymentDescription);
        testEntity.setAmount(paymentAmount);
        return testEntity;
    }

}

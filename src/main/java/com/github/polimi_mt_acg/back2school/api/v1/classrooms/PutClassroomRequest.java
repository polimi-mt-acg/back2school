package com.github.polimi_mt_acg.back2school.api.v1.classrooms;

public class PutClassroomRequest {
  private String name;
  private int floor;
  private String building;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getFloor() {
    return floor;
  }

  public void setFloor(int floor) {
    this.floor = floor;
  }

  public String getBuilding() {
    return building;
  }

  public void setBuilding(String building) {
    this.building = building;
  }
}

package com.awaissaikhu.cloudconnectedaccelometer;

import org.json.JSONObject;

import java.util.ArrayList;

public class User {
  public   String name;
  public   String course;
  public   String year;
  public   String accelerometer_data;
  public User(){

  }
    public User(String name,String course,String year,String accelerometer_data){
        this.name=name;
        this.course=course;
        this.year=year;
        this.accelerometer_data=accelerometer_data;

    }


}

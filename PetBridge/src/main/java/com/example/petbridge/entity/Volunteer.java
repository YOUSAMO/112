package com.example.petbridge.entity;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString

public class Volunteer {

    private int id;
    private String u_id;
    private String shelter;
    private LocalDate volunteer_date;
    private String volunteer_time;
    private String volunteer_area;
    private String motivation;
    private int agreement;
    private String guardianName;
    private LocalDate datetime;




}

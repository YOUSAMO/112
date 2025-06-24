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
    private LocalDate volunteerDate;
    private String volunteerTime;
    private String volunteerArea;
    private String motivation;
    private int agreement;
    private String guardianName;
    private LocalDate createdAt;




}

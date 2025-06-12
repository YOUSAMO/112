package com.example.member.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter


public class Adoption_application {

    private int id;
    private String u_id;
    private String motivation;
    private String housing;
    private String housing_type;
    private String pet_allowed;
    private String family_info;
    private String allergy_info;
    private String current_pets;
    private String experience;
    private String job;
    private String work_type;
    private String pet_care_plan;
    private String financial_status;
    private String animal_type;
    private String animal_detail;
    private String care_time;
    private String exercise_plan;
    private String travel_plan;
    private int agreement;
    private LocalDate created_at;
    private Integer pledge;
    private Long animal_id;

}

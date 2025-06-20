package com.example.petbridge.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter


public class Adoption_application {

    private int id;
    private String u_id;
    private String motivation;
    private String housingType;
    private String petAllowed;
    private String familyInfo;
    private String allergyInfo;
    private String currentPets;
    private String experience;
    private String job;
    private String workType;
    private String petCarePlan;
    private String financialStatus;
    private String animalType;
    private String animalDetail;
    private String careTime;
    private String exercisePlan;
    private String travelPlan;
    private Integer agreement;
    private LocalDateTime createdAt; // 또는 String createdAt;
    private Integer pledge;
    private String housing;
    private Long animal_id;

}

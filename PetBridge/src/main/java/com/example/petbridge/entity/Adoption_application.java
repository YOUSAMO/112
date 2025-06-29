package com.example.petbridge.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Adoption_application {

    private Long id;
    private String uId;
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

    // --- [최종 수정] Integer에서 Boolean으로 변경 ---
    private Boolean agreement;

    private LocalDateTime createdAt;

    // --- [최종 수정] Integer에서 Boolean으로 변경 ---
    private Boolean pledge;

    private String housing;
    private Long animal_id;
    private String animalName;
    private String status;

}
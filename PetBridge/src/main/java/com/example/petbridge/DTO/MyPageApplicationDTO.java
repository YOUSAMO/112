package com.example.petbridge.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class MyPageApplicationDTO {

    // 입양 신청서 정보
    private Long adoptionId;
    private String status;
    private LocalDateTime createdAt;


    // 동물 정보
    private Long animalId;
    private String animalName;
    private String animalSpecies;
    private Integer animalAge;
    private String animalGender;

    // 사용자 정보
    private String applicantId;
    private String applicantName;
    private String applicantEmail;

}

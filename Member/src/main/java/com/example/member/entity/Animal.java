package com.example.member.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
public class Animal {


    private Long id;         // 고유 ID
    private String name;       // 이름
    private String species;    // 종류
    private Integer age;       // 나이
    private String description; // 설명
    private String gender; //성별
    private boolean vaccinated; // 접종 유무
    private boolean neutered;// 중성화 유무
    private LocalDate arrivalDate; // 센터에 온 날짜
    private String likes; // 좋아하는거
    private String dislikes; // 싫어하는거


}
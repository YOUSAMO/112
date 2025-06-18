package com.example.petbridge.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Faq {


    private Long faqNo;
    private String question;
    private String answer;
    private LocalDateTime createdAt;


}
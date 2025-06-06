package com.example.member.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

@Getter
@Setter
public class LostFoundAnimal {


    private Long id;
    private String boardType;
    private String title;
    private String content;
    private String animalType;
    private String gender;
    private String age;
    private String location;
    private Date eventDate;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttachmentFile> attachments;


}
package com.example.animal.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

@Getter
@Setter
public class LostFoundAnimal {
    private Long id;
    private String userId;
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

    // DB에 저장되지 않는 임시 필드
    private boolean isLikedByCurrentUser = false;
    private String thumbnailFileName;
    private String authorName;
}
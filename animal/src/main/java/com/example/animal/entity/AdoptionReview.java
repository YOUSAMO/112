package com.example.animal.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List; // List import

@Getter
@Setter
@ToString
public class AdoptionReview {
    private Long arNo;
    private Long uNo;
    private String reviewContent;
    private LocalDateTime createdAt; // DB에서 자동 생성되거나, 서비스에서 설정
    private int viewCount;
    private int likeCount;

    private List<AttachmentFile> attachments; // 첨부파일 목록





}
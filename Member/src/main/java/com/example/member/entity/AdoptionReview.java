package com.example.member.entity;

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
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;

    private List<AttachmentFile> attachments; // 첨부파일 목록





}
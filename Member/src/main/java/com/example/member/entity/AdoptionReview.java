package com.example.member.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdoptionReview {
    private Long arNo;
    private String arTitle;     // ★★★ 입양 후기 제목 필드 ★★★
    private String authorUid;
    private String authorName;
    private String reviewContent;
    private LocalDateTime createdAt;
    private Integer viewCount;
    private Integer likeCount;
    private List<AttachmentFile> attachments = new ArrayList<>();
    private boolean likedByCurrentUser;
}
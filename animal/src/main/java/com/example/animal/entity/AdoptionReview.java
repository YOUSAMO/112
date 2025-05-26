package com.example.animal.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdoptionReview {
    private Long arNo;
    private Long uNo;
    private String reviewContent;
    private LocalDateTime createdAt;
    private int viewCount;
    private int likeCount;

}

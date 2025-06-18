package com.example.petbridge.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLike {


    private Long likeId;
    private String userId;     // users.u_id
    private Long boardId;    // 게시물의 ID (b_no)
    private String boardType;  // 게시판 타입
    private LocalDateTime likedAt;


}
package com.example.animal.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Board {
    private Long bNo;          // 게시글 고유 ID
    private String bTitle;     // 게시글 제목
    private String bContent;   // 게시글 내용
    private String bAuthor;    // 게시글 작성자 이름 또는 ID
    private LocalDateTime bDate;   // 게시글 작성일
    private int viewCount;     // 조회수
}


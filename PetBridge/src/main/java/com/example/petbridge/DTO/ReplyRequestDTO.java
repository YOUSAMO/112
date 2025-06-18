package com.example.petbridge.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyRequestDTO {
    private Long cmNo;       // 대댓글이 달릴 댓글 번호
    private String rpContent; // 대댓글 내용
}
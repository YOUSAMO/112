package com.example.petbridge.DTO;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class CommentRequestDTO {

    private Long postId;
    private Long arNo;      // 입양 후기용 ID (이 필드가 있어야 프론트에서 보낸 arNo를 받음)
    private String cmContent;
    private String boardType; // 게시판 타입을 받을 필드

}

package com.example.petbridge.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.sql.Timestamp; // Timestamp 타입을 유지합니다.

@Getter
@Setter
@ToString
public class Comment {
    private Long cmNo; // 댓글 고유 번호
    private Long postId; // 댓글이 달린 게시글 ID
    private String boardType; // 댓글이 속한 게시판 타입
    private String authorUid; // 댓글 작성자의 UID (users.u_id 참조)
    private String cmWriter; // 댓글 작성자의 이름 (표시용)
    private String cmContent; // 댓글 내용
    private Timestamp createdAt; // 작성일
    // 조인해서 가져올 사용자 이름 (엔티티에 직접 매핑하지 않고 DTO에서 사용하거나, 여기에 transient로 두어도 됨)
    // 매퍼에서 users.u_name을 authorNameDisplay로 가져올 때 사용합니다.
    private String authorNameDisplay;
}
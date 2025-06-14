package com.example.member.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.sql.Timestamp; // 또는 java.time.LocalDateTime

@Getter
@Setter
@ToString
public class Reply {
    private Long rpNo; // 대댓글 고유 번호
    private Long cmNo; // 대댓글이 달린 댓글 ID (comment.cm_no 참조)
    private String userId; // 대댓글 작성자의 UID (users.u_id 참조)
    private String rpContent; // 대댓글 내용
    private Timestamp createdAt; // 작성일
    private Timestamp updatedAt; // 수정일

    // 조인해서 가져올 사용자 이름 (엔티티에 직접 매핑하지 않고 DTO에서 사용하거나, 여기에 transient로 두어도 됨)
    // 매퍼에서 users.u_name을 userNameDisplay로 가져올 때 사용합니다.
    private String userNameDisplay;
}
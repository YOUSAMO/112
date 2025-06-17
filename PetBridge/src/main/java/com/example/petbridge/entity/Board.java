package com.example.petbridge.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor // 이 생성자는 attachments를 new ArrayList<>()로 초기화합니다.
@AllArgsConstructor // 이 생성자를 사용할 경우, attachments 파라미터에 null이 아닌 리스트를 전달해야 합니다.
public class Board {
    private Long bNo;          // 게시글 고유 ID
    private String bTitle;     // 게시글 제목
    private String bContent;   // 게시글 내용
    private String authorUid;    // 변경 후: 게시글 작성자 u_id (users 테이블의 u_id 참조)
    private String authorName;   // 추가: 화면 표시용 작성자 이름 (users 테이블의 u_name) - MyBatis JOIN으로 채워짐
    private LocalDateTime bDate;   // 게시글 작성일
    private int viewCount;     // 조회수
    private int likeCount;     // 좋아요

    // ★★★ 수정: attachments 필드를 빈 리스트로 초기화 ★★★
    private List<AttachmentFile> attachments = new ArrayList<>();  // 첨부파일 리스트
}
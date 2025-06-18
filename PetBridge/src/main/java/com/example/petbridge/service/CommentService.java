package com.example.petbridge.service;

import com.example.petbridge.DTO.CommentRequestDTO;
import com.example.petbridge.entity.Board;
import com.example.petbridge.entity.Comment;
import com.example.petbridge.entity.AdoptionReview;
import com.example.petbridge.entity.LostFoundAnimal; // LostFoundAnimal 엔티티 임포트 추가
// import com.example.member.entity.Animal; // <-- Animal 엔티티는 더 이상 직접 사용되지 않으므로 제거 가능 (필요 없으면)

import com.example.petbridge.repository.BoardRepository;
import com.example.petbridge.repository.CommentRepository;
import com.example.petbridge.repository.ReplyRepository;
import com.example.petbridge.repository.AdoptionReviewRepository;
// import com.example.member.repository.AnimalRepository; // <-- AnimalRepository는 더 이상 직접 사용되지 않으므로 제거 가능 (필요 없으면)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
// import java.util.Optional; // Optional은 더 이상 직접 사용되지 않으므로 제거 가능 (필요 없으면)

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository; // 답글 기능을 사용한다면 유지
    private final BoardRepository boardRepository; // 일반 게시판 댓글 처리용
    private final AdoptionReviewRepository adoptionReviewRepository; // 입양 후기 댓글 처리용
    private final LostFoundAnimalService lostFoundAnimalService; // ★★★ LostFoundAnimalService 주입 (가장 중요) ★★★
    // private final AnimalRepository animalRepository; // <-- 이 줄은 삭제 또는 주석 처리 (댓글 처리에서 직접 사용하지 않음)

    // 댓글 추가
    @Transactional
    public Comment addComment(CommentRequestDTO commentDTO, String loggedInUserUid, String loggedInUserName) {
        Long targetPostId;
        Object postEntity = null; // 조회된 게시글 엔티티 (유효성 검사용)

        if ("board".equals(commentDTO.getBoardType())) {
            // 일반 게시판 댓글 처리
            if (commentDTO.getPostId() == null) {
                throw new IllegalArgumentException("일반 게시글 댓글 작성 시 postId는 필수입니다.");
            }
            Board board = boardRepository.findById(commentDTO.getPostId());
            if (board == null) {
                throw new IllegalArgumentException("댓글을 작성할 게시글을 찾을 수 없습니다. 게시글 ID: " + commentDTO.getPostId());
            }
            targetPostId = commentDTO.getPostId();
            postEntity = board;

        } else if ("review".equals(commentDTO.getBoardType()) || "adoptionReview".equals(commentDTO.getBoardType())) {
            // 입양 후기 댓글 처리
            if (commentDTO.getArNo() == null) {
                throw new IllegalArgumentException("입양 후기 댓글 작성 시 arNo는 필수입니다.");
            }
            AdoptionReview review = adoptionReviewRepository.findReviewById(commentDTO.getArNo())
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 작성할 입양 후기를 찾을 수 없습니다. 입양 후기 ID: " + commentDTO.getArNo()));
            targetPostId = commentDTO.getArNo();
            postEntity = review;

        } else if ("lostfound".equals(commentDTO.getBoardType()) || "실종".equals(commentDTO.getBoardType()) || "missing".equals(commentDTO.getBoardType())) {
            // ★★★ 실종/찾아주세요 게시판 댓글 처리 (수정됨) ★★★
            if (commentDTO.getPostId() == null) {
                throw new IllegalArgumentException("실종/찾아주세요 게시글 댓글 작성 시 postId는 필수입니다.");
            }
            // LostFoundAnimalService를 사용하여 게시글 조회
            LostFoundAnimal lostFoundAnimal = lostFoundAnimalService.get(commentDTO.getPostId()); // ID만으로 조회하는 메서드 사용
            if (lostFoundAnimal == null) {
                throw new IllegalArgumentException("댓글을 작성할 실종/찾아주세요 게시글을 찾을 수 없습니다. 게시글 ID: " + commentDTO.getPostId());
            }
            targetPostId = commentDTO.getPostId();
            postEntity = lostFoundAnimal;

        } else {
            throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다: " + commentDTO.getBoardType());
        }

        // 실제 댓글 저장 로직은 게시글 유효성 검사 후 공통으로 처리
        Comment newComment = new Comment();
        newComment.setPostId(targetPostId);
        newComment.setBoardType(commentDTO.getBoardType());
        newComment.setCmContent(commentDTO.getCmContent());
        newComment.setAuthorUid(loggedInUserUid);
        newComment.setCmWriter(loggedInUserName);
        newComment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        commentRepository.insertComment(newComment);
        return newComment;
    }

    // 댓글 조회 (게시글 ID와 게시판 타입으로 필터링)
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId, String boardType) {
        return commentRepository.selectCommentsByPostId(postId, boardType);
    }

    // 댓글 수정
    @Transactional
    public Comment updateComment(Long cmNo, String content, String loggedInUserUid) {
        Comment existingComment = commentRepository.selectCommentById(cmNo);
        if (existingComment == null) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }
        if (!loggedInUserUid.equals(existingComment.getAuthorUid())) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }
        existingComment.setCmContent(content);
        existingComment.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        commentRepository.updateComment(existingComment);
        return existingComment;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long cmNo, String loggedInUserUid) {
        Comment commentToDelete = commentRepository.selectCommentById(cmNo);
        if (commentToDelete == null) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }
        if (!loggedInUserUid.equals(commentToDelete.getAuthorUid())) {
            throw new RuntimeException("댓글 삭제 권한이 없습니다.");
        }
        commentRepository.deleteComment(cmNo);
    }

    // 단일 댓글 조회
    @Transactional(readOnly = true)
    public Comment getCommentById(Long cmNo) {
        return commentRepository.selectCommentById(cmNo);
    }


}
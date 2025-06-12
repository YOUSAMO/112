package com.example.animal.service;

import com.example.animal.DTO.CommentRequestDTO;
import com.example.animal.entity.Board;
import com.example.animal.entity.Comment;
import com.example.animal.entity.AdoptionReview;
import com.example.animal.entity.Animal; // Animal 엔티티 임포트 (추가)
import com.example.animal.repository.BoardRepository;
import com.example.animal.repository.CommentRepository;
import com.example.animal.repository.ReplyRepository;
import com.example.animal.repository.AdoptionReviewRepository;
import com.example.animal.repository.AnimalRepository; // AnimalRepository 임포트 (추가)
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository;
    private final AdoptionReviewRepository adoptionReviewRepository;
    private final AnimalRepository animalRepository; // AnimalRepository 주입 (추가)

    // 댓글 추가
    @Transactional
    public Comment addComment(CommentRequestDTO commentDTO, String loggedInUserUid, String loggedInUserName) {
        Long targetPostId;

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

        } else if ("review".equals(commentDTO.getBoardType()) || "adoptionReview".equals(commentDTO.getBoardType())) {
            // 입양 후기 댓글 처리
            if (commentDTO.getArNo() == null) {
                throw new IllegalArgumentException("입양 후기 댓글 작성 시 arNo는 필수입니다.");
            }
            adoptionReviewRepository.findReviewById(commentDTO.getArNo())
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 작성할 입양 후기를 찾을 수 없습니다. 입양 후기 ID: " + commentDTO.getArNo()));
            targetPostId = commentDTO.getArNo();

        } else if ("lostfound".equals(commentDTO.getBoardType()) || "실종".equals(commentDTO.getBoardType()) || "missing".equals(commentDTO.getBoardType())) { // ★★★ 추가된 부분 ★★★
            // 실종/찾아주세요 게시판 댓글 처리
            // animal.id를 postId로 사용한다고 가정합니다.
            if (commentDTO.getPostId() == null) {
                throw new IllegalArgumentException("실종/찾아주세요 게시글 댓글 작성 시 postId는 필수입니다.");
            }
            // AnimalRepository.findById()가 Optional<Animal>을 반환한다고 가정
            animalRepository.findById(commentDTO.getPostId())
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 작성할 실종/찾아주세요 게시글을 찾을 수 없습니다. 게시글 ID: " + commentDTO.getPostId()));
            targetPostId = commentDTO.getPostId();

        } else {
            throw new IllegalArgumentException("지원하지 않는 게시판 타입입니다: " + commentDTO.getBoardType());
        }

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
        // 이 메서드에서도 boardType에 따라 실제 게시글 존재 여부를 다시 확인할 수 있습니다.
        // 현재는 commentRepository.selectCommentsByPostId가 모든 boardType의 postId를 처리한다고 가정합니다.
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
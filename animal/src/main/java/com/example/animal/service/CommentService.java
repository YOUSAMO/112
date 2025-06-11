package com.example.animal.service;

import com.example.animal.DTO.CommentRequestDTO;
import com.example.animal.entity.Board; // Board 엔티티 임포트
import com.example.animal.entity.Comment;
import com.example.animal.repository.BoardRepository; // BoardRepository 임포트
import com.example.animal.repository.CommentRepository;
import com.example.animal.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ReplyRepository replyRepository;
    private final BoardRepository boardRepository; // BoardRepository 주입

    @Transactional
    public void addComment(CommentRequestDTO commentDTO, String loggedInUserUid, String loggedInUserName) {
        // CommentRequestDTO의 postId가 이제 board.b_no를 의미합니다.
        // 해당 게시글이 존재하는지 boardRepository를 통해 확인합니다.
        Board board = boardRepository.findById(commentDTO.getPostId());
        if (board == null) {
            throw new IllegalArgumentException("댓글을 작성할 게시글을 찾을 수 없습니다. 게시글 ID: " + commentDTO.getPostId());
        }

        Comment newComment = new Comment();
        // 게시글 ID를 commentDTO에서 직접 가져와 설정합니다.
        // board.getBNo()를 다시 호출할 필요 없이, 이미 검증된 commentDTO.getPostId()를 사용합니다.
        newComment.setPostId(commentDTO.getPostId());

        newComment.setCmContent(commentDTO.getCmContent());
        newComment.setAuthorUid(loggedInUserUid);
        newComment.setCmWriter(loggedInUserName);

        commentRepository.insertComment(newComment);
    }

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        // 여기서 postId는 이제 Board의 b_no로 전달되어야 합니다.
        return commentRepository.selectCommentsByPostId(postId);
    }

    @Transactional
    public void updateComment(Long cmNo, String content, String loggedInUserUid) {
        Comment existingComment = commentRepository.selectCommentById(cmNo);
        if (existingComment == null) {
            throw new RuntimeException("댓글을 찾을 수 없습니다.");
        }

        if (!loggedInUserUid.equals(existingComment.getAuthorUid())) {
            throw new RuntimeException("댓글 수정 권한이 없습니다.");
        }

        existingComment.setCmContent(content);
        commentRepository.updateComment(existingComment);
    }

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

    @Transactional(readOnly = true)
    public Comment getCommentById(Long cmNo) {
        return commentRepository.selectCommentById(cmNo);
    }
}
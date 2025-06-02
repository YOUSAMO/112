package com.example.member.service;

import com.example.member.entity.AttachmentFile;
import com.example.member.entity.Board;
import com.example.member.repository.AttachmentFileRepository;
import com.example.member.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {



    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AttachmentFileRepository attachmentFileRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final String BOARD_TYPE = "board";

    public List<Board> getBoardsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Board> boards = boardRepository.findBoardsByPage(size, offset);

        if (boards != null && !boards.isEmpty()) {
            for (Board board : boards) {
                List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, board.getBNo());
                if (attachments != null && !attachments.isEmpty()) {
                    board.setAttachments(attachments);
                } else {
                    board.setAttachments(Collections.emptyList());
                }
            }
        }
        return boards;
    }

    public int getTotalBoardCount() {
        return boardRepository.countBoards();
    }

    public Board getBoardById(Long bNo) {
        Board board = boardRepository.findById(bNo);
        if (board != null) {
            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);
            board.setAttachments(attachments != null ? attachments : Collections.emptyList());
        }
        return board;
    }

    @Transactional
    public void createBoard(Board board, List<MultipartFile> files) throws IOException {
        boardRepository.insertBoard(board);
        Long bNo = board.getBNo();

        if (bNo == null) {

            throw new IllegalStateException("게시글 저장 후 ID를 가져올 수 없습니다.");
        }


        String boardFolder = "board_" + bNo;
        File uploadPath = new File(uploadDir, boardFolder);
        if (!uploadPath.exists()) {
            if (!uploadPath.mkdirs()) {

                throw new IOException("게시글 폴더 생성에 실패했습니다: " + uploadPath.getAbsolutePath());
            }

        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);


                    file.transferTo(dest);

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void updateBoard(Long boardId, Board boardDetailsFromForm, List<MultipartFile> newFiles) throws IOException {
        boardDetailsFromForm.setBNo(boardId);


        int updatedRows = boardRepository.updateBoard(boardDetailsFromForm);
        if (updatedRows == 0) {

            throw new RuntimeException("ID가 " + boardId + "인 게시글을 찾을 수 없거나 업데이트에 실패했습니다.");
        }


        if (newFiles != null && !newFiles.isEmpty()) {
            String boardFolder = "board_" + boardId;
            File uploadPath = new File(uploadDir, boardFolder);
            if (!uploadPath.exists()) {
                if (!uploadPath.mkdirs()) {
                    throw new IOException("게시글 폴더 생성에 실패했습니다 (업데이트 시): " + uploadPath.getAbsolutePath());
                }

            }

            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);


                    file.transferTo(dest);

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(boardId);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);

                }
            }
        }
    }

    @Transactional
    public void deleteBoard(Long bNo) throws IOException {
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        if (attachments != null) {
            for (AttachmentFile attach : attachments) {
                if (attach.getFilePath() != null && !attach.getFilePath().isEmpty()) {
                    File fileToDelete = new File(uploadDir, attach.getFilePath());
                    if (fileToDelete.exists()) {
                    }
                }
            }
        }

        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        boardRepository.deleteBoard(bNo);
    }

    @Transactional
    public void increaseViewCount(Long bNo) {
        boardRepository.incrementViewCount(bNo);
    }

    @Transactional
    public int increaseLikeCount(Long bNo) {
        boardRepository.increaseLikeCount(bNo);
        return boardRepository.getLikeCount(bNo);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) throws IOException {
        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }
        if (attachment.getFilePath() != null && !attachment.getFilePath().isEmpty()) {
            File fileToDelete = new File(uploadDir, attachment.getFilePath());

            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                } else {
                    throw new IOException("첨부파일의 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath());
                }
            }
        }
    }
}
package com.example.animal.service;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.Board;
import com.example.animal.repository.AttachmentFileRepository;
import com.example.animal.repository.BoardRepository;
import com.example.animal.repository.UserLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final AttachmentFileRepository attachmentFileRepository;
    private final UserLikeRepository userLikeRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final String BOARD_TYPE = "board";

    @Autowired
    public BoardService(BoardRepository boardRepository,
                        AttachmentFileRepository attachmentFileRepository,
                        UserLikeRepository userLikeRepository) {
        this.boardRepository = boardRepository;
        this.attachmentFileRepository = attachmentFileRepository;
        this.userLikeRepository = userLikeRepository;
    }

    public List<Board> getBoardsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Board> boards = boardRepository.findBoardsByPage(size, offset);

        if (boards != null && !boards.isEmpty()) {
            for (Board board : boards) {
                List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, board.getBNo());
                board.setAttachments(attachments != null ? attachments : Collections.emptyList());
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
    public void createBoard(Board board, List<MultipartFile> files, String loggedInUserUid) throws IOException {
        board.setAuthorUid(loggedInUserUid);
        boardRepository.insertBoard(board);
        Long bNo = board.getBNo();

        if (bNo == null) {
            throw new IllegalStateException("게시글 저장 후 ID(bNo)를 가져올 수 없습니다. MyBatis 설정을 확인하세요.");
        }

        File boardTypeDir = new File(uploadDir, BOARD_TYPE);
        String specificBoardFolderName = "board_" + bNo;
        File finalUploadPath = new File(boardTypeDir, specificBoardFolderName);

        if (!finalUploadPath.exists()) {
            if (!finalUploadPath.mkdirs()) {
                throw new IOException("게시글 첨부파일 폴더 생성에 실패했습니다: " + finalUploadPath.getAbsolutePath());
            }
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(finalUploadPath, saveName);
                    file.transferTo(dest);
                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename);
                    String filePathForDb = Paths.get(BOARD_TYPE, specificBoardFolderName, saveName).toString();
                    att.setFilePath(filePathForDb);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void updateBoard(Long boardId, Board boardDetailsFromForm, List<MultipartFile> newFiles, String loggedInUserUid) throws IOException {
        Board existingBoard = boardRepository.findById(boardId);
        if (existingBoard == null) {
            throw new RuntimeException("ID가 " + boardId + "인 게시글을 찾을 수 없습니다.");
        }
        if (loggedInUserUid == null || !loggedInUserUid.equals(existingBoard.getAuthorUid())) {
            throw new RuntimeException("이 게시글을 수정할 권한이 없습니다.");
        }

        existingBoard.setBTitle(boardDetailsFromForm.getBTitle());
        existingBoard.setBContent(boardDetailsFromForm.getBContent());

        boardRepository.updateBoard(existingBoard);

        // 새로운 첨부파일 저장 처리
        if (newFiles != null && !newFiles.isEmpty()) {
            File boardTypeDir = new File(uploadDir, BOARD_TYPE);
            String specificBoardFolderName = "board_" + boardId;
            File finalUploadPath = new File(boardTypeDir, specificBoardFolderName);

            if (!finalUploadPath.exists()) {
                if (!finalUploadPath.mkdirs()) {
                    throw new IOException("게시글 첨부파일 폴더 생성에 실패했습니다: " + finalUploadPath.getAbsolutePath());
                }
            }

            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(finalUploadPath, saveName);
                    file.transferTo(dest);

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(boardId);
                    att.setFileName(originalFilename);
                    String filePathForDb = Paths.get(BOARD_TYPE, specificBoardFolderName, saveName).toString();
                    att.setFilePath(filePathForDb);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void deleteBoard(Long bNo, String loggedInUserUid) throws IOException {
        Board boardToDelete = boardRepository.findById(bNo);
        if (boardToDelete == null) {
            System.out.println("삭제할 게시글을 찾을 수 없습니다. ID: " + bNo);
            return;
        }
        if (loggedInUserUid == null || !loggedInUserUid.equals(boardToDelete.getAuthorUid())) {
            throw new RuntimeException("이 게시글을 삭제할 권한이 없습니다.");
        }

        // 물리적 첨부파일 삭제
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attach : attachments) {
                if (attach.getFilePath() != null && !attach.getFilePath().isEmpty()) {
                    File fileToDelete = new File(uploadDir, attach.getFilePath());
                    if (fileToDelete.exists()) {
                        fileToDelete.delete();
                    }
                }
            }
        }

        // 게시글 삭제 전에 관련 데이터 삭제
        userLikeRepository.deleteLikesByContent(bNo, BOARD_TYPE);
        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        boardRepository.deleteBoard(bNo);

        // 물리적 폴더 삭제
        File boardTypeDir = new File(uploadDir, BOARD_TYPE);
        String specificBoardFolderName = "board_" + bNo;
        File specificBoardDir = new File(boardTypeDir, specificBoardFolderName);
        if (specificBoardDir.exists() && specificBoardDir.isDirectory()) {
            String[] filesInDir = specificBoardDir.list();
            if (filesInDir == null || filesInDir.length == 0) {
                specificBoardDir.delete();
            }
        }
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
    public void deleteAttachment(Long attachmentId, String loggedInUserUid) throws IOException {
        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }

        // 첨부파일 삭제 권한 확인
        Board board = boardRepository.findById(attachment.getBoardId());
        if (loggedInUserUid == null || !loggedInUserUid.equals(board.getAuthorUid())) {
            throw new RuntimeException("이 첨부파일을 삭제할 권한이 없습니다.");
        }

        // 물리적 파일 삭제
        if (attachment.getFilePath() != null && !attachment.getFilePath().isEmpty()) {
            File fileToDelete = new File(uploadDir, attachment.getFilePath());
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    throw new IOException("첨부파일의 물리적 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath());
                }
            }
        }

        // DB에서 첨부파일 정보 삭제
        attachmentFileRepository.deleteById(attachmentId);
    }
}

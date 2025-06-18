package com.example.petbridge.service;

import com.example.petbridge.entity.AttachmentFile;
import com.example.petbridge.entity.Board;
import com.example.petbridge.repository.AttachmentFileRepository;
import com.example.petbridge.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.access.AccessDeniedException; // Spring Security 의존성 제거
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

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AttachmentFileRepository attachmentFileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final String BOARD_TYPE = "board";

    // getBoardsByPage, getTotalBoardCount, getBoardById는 이전과 동일하게 유지 가능
    // (authorName이 MyBatis JOIN을 통해 채워져 온다고 가정)
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
        // ★★★ 현재 로그인한 사용자의 u_id를 authorUid로 설정 ★★★
        board.setAuthorUid(loggedInUserUid);

        boardRepository.insertBoard(board);
        Long bNo = board.getBNo();

        if (bNo == null) {
            throw new IllegalStateException("게시글 저장 후 ID(bNo)를 가져올 수 없습니다. MyBatis 설정을 확인하세요.");
        }

        // 파일 저장 로직 (기존과 동일)
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
        // ★★★ 권한 확인 ★★★
        Board existingBoard = boardRepository.findById(boardId);
        if (existingBoard == null) {
            throw new RuntimeException("ID가 " + boardId + "인 게시글을 찾을 수 없습니다.");
        }
        // loggedInUserUid가 null일 수 있으므로 null 체크 또는 Controller에서 선처리 필요
        if (loggedInUserUid == null || !loggedInUserUid.equals(existingBoard.getAuthorUid())) {
            // AccessDeniedException 대신 RuntimeException 사용
            throw new RuntimeException("이 게시글을 수정할 권한이 없습니다.");
        }

        existingBoard.setBTitle(boardDetailsFromForm.getBTitle());
        existingBoard.setBContent(boardDetailsFromForm.getBContent());

        int updatedRows = boardRepository.updateBoard(existingBoard);
        if (updatedRows == 0) {
            throw new RuntimeException("ID가 " + boardId + "인 게시글 업데이트에 실패했습니다.");
        }

        // 첨부파일 처리 로직 (기존과 동일)
        if (newFiles != null && !newFiles.isEmpty()) {
            File boardTypeDir = new File(uploadDir, BOARD_TYPE);
            String specificBoardFolderName = "board_" + boardId;
            File finalUploadPath = new File(boardTypeDir, specificBoardFolderName);
            if (!finalUploadPath.exists()) {
                if (!finalUploadPath.mkdirs()) {
                    throw new IOException("게시글 첨부파일 폴더 생성에 실패했습니다 (업데이트 시): " + finalUploadPath.getAbsolutePath());
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
        // ★★★ 권한 확인 ★★★
        Board boardToDelete = boardRepository.findById(bNo);
        if (boardToDelete == null) {
            System.out.println("삭제할 게시글을 찾을 수 없습니다. ID: " + bNo);
            return;
        }
        // loggedInUserUid가 null일 수 있으므로 null 체크 또는 Controller에서 선처리 필요
        if (loggedInUserUid == null || !loggedInUserUid.equals(boardToDelete.getAuthorUid())) {
            // AccessDeniedException 대신 RuntimeException 사용
            throw new RuntimeException("이 게시글을 삭제할 권한이 없습니다.");
        }

        // 첨부파일 및 폴더 삭제 로직 (기존과 동일)
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
        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        boardRepository.deleteBoard(bNo);

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
    public void deleteAttachment(Long attachmentId /*, String loggedInUserUid - 필요시 */) throws IOException {
        // 만약 첨부파일 삭제도 게시글 작성자만 가능하게 하려면,
        // AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        // Board board = boardRepository.findById(attachment.getBoardId());
        // if(loggedInUserUid == null || !loggedInUserUid.equals(board.getAuthorUid())) {
        //    throw new RuntimeException("첨부파일을 삭제할 권한이 없습니다.");
        // }

        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }

        if (attachment.getFilePath() != null && !attachment.getFilePath().isEmpty()) {
            File fileToDelete = new File(uploadDir, attachment.getFilePath());
            if (fileToDelete.exists()) {
                if (!fileToDelete.delete()) {
                    throw new IOException("첨부파일의 물리적 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath());
                }
            }
        }
        attachmentFileRepository.deleteById(attachmentId);
    }
}
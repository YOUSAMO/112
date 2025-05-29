package com.example.animal.service;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.Board;
import com.example.animal.repository.AttachmentFileRepository;
import com.example.animal.repository.BoardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections; // Collections.emptyList() 사용 가능
import java.util.List;
import java.util.UUID;

@Service
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private AttachmentFileRepository attachmentFileRepository; // 이미 주입되어 있음

    @Value("${file.upload.dir}")
    private String uploadDir;

    private final String BOARD_TYPE = "board";

    public List<Board> getBoardsByPage(int page, int size) {
        int offset = (page - 1) * size;
        List<Board> boards = boardRepository.findBoardsByPage(size, offset);

        // 각 게시글에 대한 첨부파일 정보를 가져와서 설정
        if (boards != null && !boards.isEmpty()) {
            for (Board board : boards) {
                // 해당 게시글의 첨부파일 목록을 조회합니다.
                // 성능 최적화를 위해서는 여기서 첫 번째 파일만 가져오는 쿼리를 사용하거나,
                // BoardRepository에서 JOIN을 통해 가져오는 것이 좋습니다.
                List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, board.getBNo());
                if (attachments != null && !attachments.isEmpty()) {
                    board.setAttachments(attachments); // Board 객체에 첨부파일 리스트 설정
                } else {
                    board.setAttachments(Collections.emptyList()); // 첨부파일이 없는 경우 빈 리스트 설정
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

    // ... (createBoard, updateBoard, deleteBoard, increaseViewCount, increaseLikeCount, deleteAttachment 메소드는 이전과 동일) ...
    @Transactional
    public void createBoard(Board board, List<MultipartFile> files) throws IOException {
        // ... (기존 로직) ...
        boardRepository.insertBoard(board);
        Long bNo = board.getBNo();

        if (bNo == null) {
            log.error("게시글 저장 후 ID를 가져올 수 없습니다. Board: {}", board);
            throw new IllegalStateException("게시글 저장 후 ID를 가져올 수 없습니다.");
        }
        log.info("게시글 생성됨: ID={}", bNo);

        String boardFolder = "board_" + bNo;
        File uploadPath = new File(uploadDir, boardFolder);
        if (!uploadPath.exists()) {
            if (!uploadPath.mkdirs()) {
                log.error("게시글 폴더 생성 실패: {}", uploadPath.getAbsolutePath());
                throw new IOException("게시글 폴더 생성에 실패했습니다: " + uploadPath.getAbsolutePath());
            }
            log.info("게시글 폴더 생성됨: {}", uploadPath.getAbsolutePath());
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);

                    log.info("파일 저장 시도: {}", dest.getAbsolutePath());
                    file.transferTo(dest);

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                    log.info("첨부파일 DB 저장됨: {}, Board ID: {}", originalFilename, bNo);
                }
            }
        }
    }

    @Transactional
    public void updateBoard(Long boardId, Board boardDetailsFromForm, List<MultipartFile> newFiles) throws IOException {
        // ... (기존 로직) ...
        boardDetailsFromForm.setBNo(boardId);
        log.info("게시글 업데이트 시도: ID={}", boardId);

        int updatedRows = boardRepository.updateBoard(boardDetailsFromForm);
        if (updatedRows == 0) {
            log.warn("게시글 업데이트 실패 또는 대상 없음: ID={}", boardId);
            throw new RuntimeException("ID가 " + boardId + "인 게시글을 찾을 수 없거나 업데이트에 실패했습니다.");
        }
        log.info("게시글 내용 업데이트 완료: ID={}", boardId);

        if (newFiles != null && !newFiles.isEmpty()) {
            String boardFolder = "board_" + boardId;
            File uploadPath = new File(uploadDir, boardFolder);
            if (!uploadPath.exists()) {
                if (!uploadPath.mkdirs()) {
                    log.error("게시글 폴더 생성 실패 (업데이트 시): {}", uploadPath.getAbsolutePath());
                    throw new IOException("게시글 폴더 생성에 실패했습니다 (업데이트 시): " + uploadPath.getAbsolutePath());
                }
                log.info("게시글 폴더 생성됨 (업데이트 시): {}", uploadPath.getAbsolutePath());
            }

            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);

                    log.info("새 첨부파일 저장 시도: {}", dest.getAbsolutePath());
                    file.transferTo(dest);

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(boardId);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + File.separator + saveName);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                    log.info("새 첨부파일 DB 저장됨: {}, Board ID: {}", originalFilename, boardId);
                }
            }
        }
    }

    @Transactional
    public void deleteBoard(Long bNo) throws IOException {
        // ... (기존 로직) ...
        log.info("게시글 삭제 시도: ID={}", bNo);
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        if (attachments != null) {
            for (AttachmentFile attach : attachments) {
                if (attach.getFilePath() != null && !attach.getFilePath().isEmpty()) {
                    File fileToDelete = new File(uploadDir, attach.getFilePath());
                    if (fileToDelete.exists()) {
                        log.info("물리적 파일 삭제 시도 (게시글 삭제 중): {}", fileToDelete.getAbsolutePath());
                        if (fileToDelete.delete()) {
                            log.info("물리적 파일 삭제 성공 (게시글 삭제 중): {}", fileToDelete.getName());
                        } else {
                            log.warn("물리적 파일 삭제 실패 (게시글 삭제 중): {}", fileToDelete.getName());
                        }
                    } else {
                        log.warn("삭제할 물리적 파일 없음 (게시글 삭제 중): {}", fileToDelete.getAbsolutePath());
                    }
                }
            }
        }

        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        log.info("첨부파일 DB 레코드 삭제 완료 (게시글 삭제 중): Board ID={}", bNo);

        boardRepository.deleteBoard(bNo);
        log.info("게시글 DB 레코드 삭제 완료: Board ID={}", bNo);
    }

    @Transactional
    public void increaseViewCount(Long bNo) {
        // ... (기존 로직) ...
        boardRepository.incrementViewCount(bNo);
    }

    @Transactional
    public int increaseLikeCount(Long bNo) {
        // ... (기존 로직) ...
        boardRepository.increaseLikeCount(bNo);
        return boardRepository.getLikeCount(bNo);
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) throws IOException {
        // ... (기존 로직) ...
        log.info("===> [START] deleteAttachment - attachmentId: {}", attachmentId);

        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            log.warn("===> [FAIL] Attachment not found in DB for attachmentId: {}", attachmentId);
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }
        log.info("===> [SUCCESS] Found attachment in DB: ID={}, FilePath='{}'", attachment.getId(), attachment.getFilePath());

        if (attachment.getFilePath() != null && !attachment.getFilePath().isEmpty()) {
            File fileToDelete = new File(uploadDir, attachment.getFilePath());
            log.info("===> Attempting to delete physical file: '{}'", fileToDelete.getAbsolutePath());

            if (fileToDelete.exists()) {
                log.info("===> Physical file exists: '{}'", fileToDelete.getAbsolutePath());
                if (fileToDelete.delete()) {
                    log.info("===> [SUCCESS] Physical file deleted: '{}'", fileToDelete.getName());
                } else {
                    log.warn("===> [FAIL] Failed to delete physical file: '{}'. Can Write? {}, Can Read? {}",
                            fileToDelete.getAbsolutePath(), fileToDelete.canWrite(), fileToDelete.canRead());
                    throw new IOException("첨부파일의 물리적 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath());
                }
            } else {
                log.warn("===> [WARN] Physical file does not exist, skipping delete: '{}'", fileToDelete.getAbsolutePath());
            }
        } else {
            log.warn("===> [WARN] FilePath is null or empty for attachmentId: {}. Skipping physical file deletion.", attachmentId);
        }

        log.info("===> Attempting to delete attachment record from DB: attachmentId={}", attachmentId);
        int deletedDbRows = attachmentFileRepository.deleteById(attachmentId);
        if (deletedDbRows > 0) {
            log.info("===> [SUCCESS] Attachment record deleted from DB: attachmentId={}, Rows affected: {}", attachmentId, deletedDbRows);
        } else {
            log.warn("===> [FAIL] Failed to delete attachment record from DB or record not found: attachmentId={}", attachmentId);
        }
        log.info("===> [END] deleteAttachment - attachmentId: {}", attachmentId);
    }
}
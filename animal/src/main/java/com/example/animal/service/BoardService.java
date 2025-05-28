package com.example.animal.service;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.Board;
import com.example.animal.repository.AttachmentFileRepository;
import com.example.animal.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

    private final String BOARD_TYPE = "board";  // 게시판 첨부파일 구분용 타입명

    public List<Board> getBoardsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return boardRepository.findBoardsByPage(size, offset);
    }

    public int getTotalBoardCount() {
        return boardRepository.countBoards();
    }

    public Board getBoardById(Long bNo) {
        Board board = boardRepository.findById(bNo);
        if (board != null) {
            List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);
            board.setAttachments(attachments);
        }
        return board;
    }

    @Transactional
    public void createBoard(Board board, List<MultipartFile> files) throws IOException {
        // 1. 게시글 저장
        boardRepository.insertBoard(board);
        Long bNo = board.getBNo();

        // 게시글별 폴더 생성
        String boardFolder = "board_" + bNo;
        File uploadPath = new File(uploadDir, boardFolder);
        if (!uploadPath.exists()) {
            boolean created = uploadPath.mkdirs();
            System.out.println("게시글 폴더 생성 성공? " + created + ", 경로: " + uploadPath.getAbsolutePath());
        }

        // 2. 첨부파일 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);

                    // 파일 저장
                    file.transferTo(dest);

                    // DB 저장 객체 생성
                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + "/" + saveName);  // 게시글 폴더 포함 경로 저장
                    att.setFileType(file.getContentType());

                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void updateBoard(Board board, List<MultipartFile> files) throws IOException {
        boardRepository.updateBoard(board);

        Long bNo = board.getBNo();

        // 기존 첨부파일 삭제 (DB + 물리적 파일 삭제)
        List<AttachmentFile> oldAttachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        for (AttachmentFile attach : oldAttachments) {
            File file = new File(uploadDir, attach.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                System.out.println("기존 첨부파일 삭제: " + deleted + ", 파일명: " + file.getAbsolutePath());
            }
        }
        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        // 게시글별 폴더 생성
        String boardFolder = "board_" + bNo;
        File uploadPath = new File(uploadDir, boardFolder);
        if (!uploadPath.exists()) {
            boolean created = uploadPath.mkdirs();
            System.out.println("게시글 폴더 생성 성공? " + created + ", 경로: " + uploadPath.getAbsolutePath());
        }

        // 새 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID() + "_" + originalFilename;
                    File dest = new File(uploadPath, saveName);

                    // 물리적 파일 저장
                    file.transferTo(dest);

                    // DB 저장 객체 생성
                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename);
                    att.setFilePath(boardFolder + "/" + saveName);  // 게시글 폴더 포함 경로 저장
                    att.setFileType(file.getContentType());

                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void deleteBoard(Long bNo) {
        // 1. 첨부파일 리스트 조회
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        // 2. 물리적 파일 삭제
        for (AttachmentFile attach : attachments) {
            File file = new File(uploadDir, attach.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                System.out.println("파일 삭제됨? " + deleted + ", 파일명: " + file.getAbsolutePath());
            }
        }

        // 3. 첨부파일 DB 레코드 삭제
        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        // 4. 게시글 삭제
        boardRepository.deleteBoard(bNo);
    }

    public void increaseViewCount(Long bNo) {
        boardRepository.incrementViewCount(bNo);
    }

    @Transactional
    public int increaseLikeCount(Long bNo) {
        boardRepository.increaseLikeCount(bNo);
        return boardRepository.getLikeCount(bNo);
    }

}

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
import java.nio.file.Paths; // Paths 사용을 위해 추가 (선택적)
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

    private final String BOARD_TYPE = "board"; // 중간 폴더 이름으로 사용

    // getBoardsByPage, getTotalBoardCount, getBoardById 메소드는 변경 없음
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
            throw new IllegalStateException("게시글 저장 후 ID(bNo)를 가져올 수 없습니다. MyBatis 설정을 확인하세요.");
        }

        // ★★★ 폴더 경로 생성 로직 수정 ★★★
        File boardTypeDir = new File(uploadDir, BOARD_TYPE); // 예: [uploadDir]/board
        String specificBoardFolderName = "board_" + bNo;     // 예: board_123
        File finalUploadPath = new File(boardTypeDir, specificBoardFolderName); // 예: [uploadDir]/board/board_123

        if (!finalUploadPath.exists()) {
            if (!finalUploadPath.mkdirs()) { // mkdirs()는 중간 폴더(board)도 필요시 생성합니다.
                throw new IOException("게시글 첨부파일 폴더 생성에 실패했습니다: " + finalUploadPath.getAbsolutePath());
            }
            System.out.println("첨부파일 폴더 생성됨: " + finalUploadPath.getAbsolutePath());
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(finalUploadPath, saveName); // 수정된 finalUploadPath 사용

                    try {
                        file.transferTo(dest);
                    } catch (IOException e) {
                        throw new IOException("첨부파일 저장에 실패했습니다: " + dest.getAbsolutePath(), e);
                    }

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(bNo);
                    att.setFileName(originalFilename); // 또는 saveName을 저장할 수도 있음

                    // ★★★ DB에 저장되는 파일 경로 수정 ★★★
                    // uploadDir를 제외한 상대 경로: "board/board_123/uuid_filename.jpg"
                    String filePathForDb = Paths.get(BOARD_TYPE, specificBoardFolderName, saveName).toString();
                    // Windows에서 File.separator 대신 Paths.get().toString() 사용 시 /로 경로 구분자가 통일될 수 있으나,
                    // File 객체로 다룰 때 문제 없음. 일관성을 위해 File.separator 사용도 가능.
                    // String filePathForDb = BOARD_TYPE + File.separator + specificBoardFolderName + File.separator + saveName;

                    att.setFilePath(filePathForDb);
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
            // ★★★ 폴더 경로 생성 로직 수정 (createBoard와 동일) ★★★
            File boardTypeDir = new File(uploadDir, BOARD_TYPE);
            String specificBoardFolderName = "board_" + boardId;
            File finalUploadPath = new File(boardTypeDir, specificBoardFolderName);

            if (!finalUploadPath.exists()) {
                if (!finalUploadPath.mkdirs()) {
                    throw new IOException("게시글 첨부파일 폴더 생성에 실패했습니다 (업데이트 시): " + finalUploadPath.getAbsolutePath());
                }
                System.out.println("첨부파일 폴더 생성됨 (업데이트 시): " + finalUploadPath.getAbsolutePath());
            }

            for (MultipartFile file : newFiles) {
                if (!file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String saveName = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(finalUploadPath, saveName); // 수정된 finalUploadPath 사용

                    try {
                        file.transferTo(dest);
                    } catch (IOException e) {
                        throw new IOException("새 첨부파일 저장에 실패했습니다: " + dest.getAbsolutePath(), e);
                    }

                    AttachmentFile att = new AttachmentFile();
                    att.setBoardType(BOARD_TYPE);
                    att.setBoardId(boardId);
                    att.setFileName(originalFilename);

                    // ★★★ DB에 저장되는 파일 경로 수정 (createBoard와 동일) ★★★
                    String filePathForDb = Paths.get(BOARD_TYPE, specificBoardFolderName, saveName).toString();
                    // String filePathForDb = BOARD_TYPE + File.separator + specificBoardFolderName + File.separator + saveName;

                    att.setFilePath(filePathForDb);
                    att.setFileType(file.getContentType());
                    attachmentFileRepository.insertAttachment(att);
                }
            }
        }
    }

    @Transactional
    public void deleteBoard(Long bNo) throws IOException {
        List<AttachmentFile> attachments = attachmentFileRepository.findByBoardTypeAndBoardId(BOARD_TYPE, bNo);

        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attach : attachments) {
                if (attach.getFilePath() != null && !attach.getFilePath().isEmpty()) {
                    // attach.getFilePath()는 "board/board_bNo/filename.jpg" 형태의 상대 경로를 가짐
                    File fileToDelete = new File(uploadDir, attach.getFilePath());
                    if (fileToDelete.exists()) {
                        if (!fileToDelete.delete()) {
                            System.err.println("게시글 삭제 중 첨부파일 삭제 실패 (파일): " + fileToDelete.getAbsolutePath());
                        } else {
                            System.out.println("게시글 삭제 중 첨부파일 삭제 성공 (파일): " + fileToDelete.getAbsolutePath());
                        }
                    }
                }
            }
        }

        attachmentFileRepository.deleteByBoardTypeAndBoardId(BOARD_TYPE, bNo);
        boardRepository.deleteBoard(bNo);

        // ★★★ (선택 사항) 게시글 삭제 후 해당 폴더도 삭제하는 로직 추가 ★★★
        // 예: [uploadDir]/board/board_bNo 폴더 삭제
        File boardTypeDir = new File(uploadDir, BOARD_TYPE);
        String specificBoardFolderName = "board_" + bNo;
        File specificBoardDir = new File(boardTypeDir, specificBoardFolderName);
        if (specificBoardDir.exists() && specificBoardDir.isDirectory()) {
            // 폴더 내 파일이 모두 삭제되었는지 확인 후 삭제하는 것이 안전합니다.
            // (위에서 개별 파일을 삭제했으므로 비어있을 가능성이 높음)
            String[] filesInDir = specificBoardDir.list();
            if (filesInDir == null || filesInDir.length == 0) { // 폴더가 비었거나, 접근 오류가 없는 경우
                if (specificBoardDir.delete()) {
                    System.out.println("게시글 폴더 삭제 성공: " + specificBoardDir.getAbsolutePath());
                    // (선택적) 만약 [uploadDir]/board 폴더도 비었다면 그것도 삭제할지 고려 가능
                    if (boardTypeDir.exists() && boardTypeDir.isDirectory()) {
                        String[] filesInBoardTypeDir = boardTypeDir.list();
                        if (filesInBoardTypeDir == null || filesInBoardTypeDir.length == 0) {
                            if (boardTypeDir.delete()) {
                                System.out.println("상위 게시판 타입 폴더 삭제 성공: " + boardTypeDir.getAbsolutePath());
                            } else {
                                System.err.println("상위 게시판 타입 폴더 삭제 실패: " + boardTypeDir.getAbsolutePath());
                            }
                        }
                    }
                } else {
                    System.err.println("게시글 폴더 삭제 실패: " + specificBoardDir.getAbsolutePath());
                }
            } else {
                System.err.println("게시글 폴더(" + specificBoardDir.getAbsolutePath() + ")가 비어있지 않아 삭제하지 않음. 남은 파일: " + (filesInDir != null ? String.join(", ", filesInDir) : "확인 불가"));
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
    public void deleteAttachment(Long attachmentId) throws IOException {
        AttachmentFile attachment = attachmentFileRepository.findById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일을 찾을 수 없습니다.");
        }

        if (attachment.getFilePath() != null && !attachment.getFilePath().isEmpty()) {
            // attachment.getFilePath()는 "board/board_bNo/filename.jpg" 형태의 상대 경로
            File fileToDelete = new File(uploadDir, attachment.getFilePath());
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    System.out.println("파일 시스템에서 첨부파일 삭제 성공: " + fileToDelete.getAbsolutePath());
                } else {
                    throw new IOException("첨부파일의 물리적 파일 삭제에 실패했습니다: " + fileToDelete.getAbsolutePath());
                }
            } else {
                System.out.println("삭제할 첨부파일이 파일 시스템에 이미 존재하지 않습니다: " + fileToDelete.getAbsolutePath());
            }
        } else {
            System.out.println("첨부파일 정보에 파일 경로가 없습니다. ID: " + attachmentId);
        }

        int deletedDbRows = attachmentFileRepository.deleteById(attachmentId);
        if (deletedDbRows == 0) {
            System.err.println("DB에서 첨부파일 정보 삭제 실패 (아마도 이미 삭제됨). ID: " + attachmentId);
            throw new RuntimeException("ID가 " + attachmentId + "인 첨부파일 정보를 DB에서 삭제하는데 실패했습니다.");
        }
        System.out.println("DB에서 첨부파일 정보 삭제 성공. ID: " + attachmentId);
    }
}
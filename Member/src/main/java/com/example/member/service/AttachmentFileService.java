package com.example.member.service;

import com.example.member.entity.AttachmentFile;
import com.example.member.repository.AttachmentFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;

@Service
public class AttachmentFileService {

    private final AttachmentFileRepository repository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    public AttachmentFileService(AttachmentFileRepository repository) {
        this.repository = repository;
    }

    public List<AttachmentFile> getFiles(String boardType, Long boardId) {
        return repository.findByBoardTypeAndBoardId(boardType, boardId);
    }

    // 게시글 전체 삭제 시 모든 관련 파일을 삭제하는 메소드
    public void deleteFilesByBoard(String boardType, Long boardId) {
        List<AttachmentFile> filesToDelete = repository.findByBoardTypeAndBoardId(boardType, boardId);
        for (AttachmentFile file : filesToDelete) {
            // 아래 수정된 deleteFile 메소드를 재사용
            deleteFile(file.getId());
        }
    }

    // ★★★★★ 개별 파일 삭제 로직 수정 ★★★★★
    @Transactional
    public void deleteFile(Long attachmentId) {
        // 1. DB에서 첨부파일 정보를 조회합니다.
        AttachmentFile file = repository.findById(attachmentId);

        if (file != null) {
            // 2. DB에 저장된 웹 경로(예: /uploads/...)를 가져옵니다.
            String dbFilePath = file.getFilePath();

            // 3. 웹 경로에서 '/uploads/' 부분을 제거하여 실제 파일 시스템의 상대 경로를 만듭니다.
            String relativePath = dbFilePath.startsWith("/uploads/")
                    ? dbFilePath.substring(9) // "/uploads/" 길이(9)만큼 잘라냄
                    : dbFilePath;

            // 4. 기본 업로드 폴더와 상대 경로를 합쳐 완전한 물리적 파일 경로를 생성합니다.
            File physicalFile = new File(uploadDir + relativePath);

            // 5. 물리적 파일이 존재하면 삭제합니다.
            if (physicalFile.exists()) {
                if (!physicalFile.delete()) {
                    System.err.println("파일 삭제 실패: " + physicalFile.getAbsolutePath());
                }
            } else {
                System.err.println("삭제할 파일이 존재하지 않음: " + physicalFile.getAbsolutePath());
            }

            // 6. DB에서 파일 정보를 삭제합니다.
            repository.deleteById(attachmentId);
        }
    }
}
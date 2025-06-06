package com.example.member.service;

import com.example.member.entity.AttachmentFile;
import com.example.member.entity.LostFoundAnimal;
import com.example.member.repository.LostFoundAnimalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class LostFoundAnimalService {

    private final LostFoundAnimalRepository repository;
    private static final String BOARD_TYPE_NAME = "lostfound"; // 기본 상위 폴더 이름
    // ★★★ 폴더 이름 접두사 수정 ★★★
    private static final String FOLDER_PREFIX = "LostFound_"; // "post_"에서 "LostFound_"로 변경

    @Value("${file.upload.dir}")
    private String uploadDir;

    public LostFoundAnimalService(LostFoundAnimalRepository repository) {
        this.repository = repository;
    }

    // getList, get, register, modify 메소드는 변경 없음 (내부적으로 saveAttachments 호출)
    public List<LostFoundAnimal> getList() {
        return repository.findAll();
    }

    public LostFoundAnimal get(Long id) {
        LostFoundAnimal animal = repository.findById(id);
        if (animal != null) {
            List<AttachmentFile> attachments = repository.findAttachmentsByAnimalId(id);
            animal.setAttachments(attachments);
        }
        return animal;
    }

    public void register(LostFoundAnimal animal, List<MultipartFile> files) throws IOException {
        repository.insert(animal);
        saveAttachments(animal.getId(), files);
    }

    @Transactional
    public void modify(LostFoundAnimal animal, List<MultipartFile> files) throws IOException {
        repository.update(animal);
        saveAttachments(animal.getId(), files);
    }

    @Transactional
    public void remove(Long id) {
        List<AttachmentFile> attachments = repository.findAttachmentsByAnimalId(id);

        if (attachments != null && !attachments.isEmpty()) {
            for (AttachmentFile attachment : attachments) {
                File file = new File(attachment.getFilePath()); // getFilePath()는 절대 경로를 반환
                if (file.exists()) {
                    if (file.delete()) {
                        System.out.println("파일 삭제 성공: " + attachment.getFilePath());
                    } else {
                        System.err.println("파일 삭제 실패: " + attachment.getFilePath());
                    }
                } else {
                    System.out.println("삭제할 파일 없음 (이미 삭제되었거나 경로 오류): " + attachment.getFilePath());
                }
            }
        }

        repository.deleteAttachmentsByAnimalId(id);
        System.out.println(id + "번 게시물의 모든 첨부파일 DB 레코드 삭제 시도 완료.");

        // 폴더 이름 생성 시 FOLDER_PREFIX 사용
        String specificFolderName = FOLDER_PREFIX + id; // 예: "LostFound_6"
        File animalSpecificDir = new File(new File(uploadDir, BOARD_TYPE_NAME), specificFolderName);

        if (animalSpecificDir.exists() && animalSpecificDir.isDirectory()) {
            if (animalSpecificDir.delete()) {
                System.out.println("폴더 삭제 성공: " + animalSpecificDir.getAbsolutePath());
            } else {
                System.err.println("폴더 삭제 실패 (폴더가 비어있지 않거나 권한 문제): " + animalSpecificDir.getAbsolutePath());
                String[] remainingFiles = animalSpecificDir.list();
                if (remainingFiles != null && remainingFiles.length > 0) {
                    System.err.println("폴더(" + animalSpecificDir.getAbsolutePath() + ") 내 남은 파일/폴더들: " + String.join(", ", remainingFiles));
                }
            }
        } else {
            System.out.println("삭제할 폴더 없음: " + animalSpecificDir.getAbsolutePath());
        }

        repository.deleteById(id);
        System.out.println(id + "번 게시물 DB 레코드 삭제 시도 완료.");
    }

    // increaseViewCount, increaseLikeCount 메소드는 변경 없음
    public void increaseViewCount(Long id) {
        repository.increaseViewCount(id);
    }

    public void increaseLikeCount(Long id) {
        repository.increaseLikeCount(id);
    }


    private void saveAttachments(Long animalId, List<MultipartFile> files) throws IOException {
        if (files != null && !files.isEmpty()) {
            File boardBaseDir = new File(uploadDir, BOARD_TYPE_NAME); // 예: [uploadDir]/lostfound

            // 폴더 이름 생성 시 FOLDER_PREFIX 사용
            String specificFolderName = FOLDER_PREFIX + animalId; // 예: "LostFound_6"
            File animalSpecificDir = new File(boardBaseDir, specificFolderName); // 예: [uploadDir]/lostfound/LostFound_6

            if (!animalSpecificDir.exists()) {
                animalSpecificDir.mkdirs(); // 폴더 생성
            }

            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String originalFilename = file.getOriginalFilename();
                    String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                    File dest = new File(animalSpecificDir, storedFilename); // 최종 저장 파일 객체
                    file.transferTo(dest); // 파일 저장

                    AttachmentFile attachment = new AttachmentFile();
                    attachment.setBoardType(BOARD_TYPE_NAME);
                    attachment.setBoardId(animalId);
                    attachment.setFileName(storedFilename);
                    // filePath는 절대 경로로 저장되므로, 폴더명 변경이 자동으로 반영됩니다.
                    attachment.setFilePath(dest.getAbsolutePath());
                    attachment.setFileType(file.getContentType());
                    repository.insertAttachment(attachment);
                }
            }
        }
    }

    // deleteSingleAttachment 메소드는 attachment.getFilePath()를 사용하므로 직접적인 변경은 불필요.
    // getFilePath()가 saveAttachments에서 올바른 절대 경로를 저장했다면 정상 동작합니다.
    @Transactional
    public boolean deleteSingleAttachment(Long attachmentId) {
        AttachmentFile attachment = repository.findAttachmentById(attachmentId);
        if (attachment == null) {
            System.err.println("삭제할 첨부파일 정보를 DB에서 찾을 수 없습니다. Attachment ID: " + attachmentId);
            return false;
        }

        File physicalFile = new File(attachment.getFilePath());
        boolean fileActuallyDeleted = false;
        if (physicalFile.exists()) {
            if (physicalFile.delete()) {
                System.out.println("물리적 파일 삭제 성공: " + attachment.getFilePath());
                fileActuallyDeleted = true;
            } else {
                System.err.println("물리적 파일 삭제 실패: " + attachment.getFilePath());
                return false;
            }
        } else {
            System.out.println("삭제할 물리적 파일이 이미 존재하지 않음: " + attachment.getFilePath());
            fileActuallyDeleted = true;
        }

        if (fileActuallyDeleted) {
            int deletedRows = repository.deleteSingleAttachmentById(attachmentId);
            if (deletedRows > 0) {
                System.out.println("첨부파일 DB 레코드 삭제 성공. Attachment ID: " + attachmentId);
                return true;
            } else {
                System.err.println("첨부파일 DB 레코드 삭제 실패 ( 영향받은 행 없음). Attachment ID: " + attachmentId);
                return false;
            }
        }
        return false;
    }
}
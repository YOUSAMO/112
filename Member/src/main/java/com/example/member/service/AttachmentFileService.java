package com.example.member.service;

import com.example.member.entity.AttachmentFile;
import com.example.member.repository.AttachmentFileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttachmentFileService {

    private final AttachmentFileRepository repository;

    public AttachmentFileService(AttachmentFileRepository repository) {
        this.repository = repository;
    }

    public void saveFile(AttachmentFile file) {
        repository.insertAttachment(file);  // 메서드명 맞춤
    }

    public List<AttachmentFile> getFiles(String boardType, Long boardId) {
        return repository.findByBoardTypeAndBoardId(boardType, boardId);  // 메서드명 맞춤
    }

    public void deleteFiles(String boardType, Long boardId) {
        repository.deleteByBoardTypeAndBoardId(boardType, boardId);  // 메서드명 맞춤
    }


}
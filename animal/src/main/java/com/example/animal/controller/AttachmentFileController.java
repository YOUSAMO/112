package com.example.animal.controller;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.service.AttachmentFileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/files")
public class AttachmentFileController {

    private final AttachmentFileService service;

    public AttachmentFileController(AttachmentFileService service) {
        this.service = service;
    }

    // 첨부파일 목록 조회
    @GetMapping("/{boardType}/{boardId}")
    public List<AttachmentFile> getFiles(@PathVariable String boardType, @PathVariable Long boardId) {
        return service.getFiles(boardType, boardId);
    }

}

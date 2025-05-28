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

//    // 첨부파일 삭제 처리
//    @PostMapping("/attachments/{id}/delete")
//    public String deleteAttachment(@PathVariable Long id, @RequestHeader("Referer") String referer) {
//        service.deleteAttachmentById(id);
//        return "redirect:" + referer; // 현재 페이지로 리다이렉트
//    }
}

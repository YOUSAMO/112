package com.example.animal.entity;



import lombok.Data;

@Data
public class AttachmentFile {
    private Long id;
    private String boardType;
    private Long boardId;
    private String fileName;
    private String filePath;
    private String fileType;
}


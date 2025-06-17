package com.example.petbridge.repository;

import com.example.petbridge.entity.AttachmentFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttachmentFileRepository {


    void insertAttachment(AttachmentFile attachmentFile);
    List<AttachmentFile> findByBoardTypeAndBoardId(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    void deleteByBoardTypeAndBoardId(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    AttachmentFile findById(Long id);
    int deleteById(Long id);



}
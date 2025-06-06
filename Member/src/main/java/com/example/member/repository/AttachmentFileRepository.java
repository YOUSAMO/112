package com.example.member.repository;

import com.example.member.entity.AttachmentFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttachmentFileRepository {
    void insertAttachment(AttachmentFile attachmentFile);
    List<AttachmentFile> findByBoardTypeAndBoardId(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    void deleteByBoardTypeAndBoardId(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    AttachmentFile findById(Long id); // 개별 첨부파일 PK로 조회
    int deleteById(Long id);      // 개별 첨부파일 PK로 삭제


}
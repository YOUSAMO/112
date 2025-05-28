package com.example.animal.repository;

import com.example.animal.entity.AttachmentFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AttachmentFileRepository {

    List<AttachmentFile> findByBoardTypeAndBoardId(String boardType, Long boardId);

    void insertAttachment(AttachmentFile attachmentFile);

    void deleteByBoardTypeAndBoardId(String boardType, Long boardId);

    List<AttachmentFile> findByBoardId(Long bNo);

    void deleteByBoardId(Long bNo);
}

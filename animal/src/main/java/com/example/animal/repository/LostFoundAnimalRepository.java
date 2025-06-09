package com.example.animal.repository;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.LostFoundAnimal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LostFoundAnimalRepository {
    int insert(LostFoundAnimal animal);

    LostFoundAnimal findById(Long id);

    List<LostFoundAnimal> findAll();

    int update(LostFoundAnimal animal);

    int deleteById(Long id);

    int increaseViewCount(Long id);

    int increaseLikeCount(Long id);

    int insertAttachment(AttachmentFile file);

    List<AttachmentFile> findAttachmentsByAnimalId(Long animalId);

    void deleteAttachmentsByAnimalId(Long animalId);

    AttachmentFile findAttachmentById(Long attachmentId); // 첨부파일 ID로 특정 첨부파일 정보 조회

    int deleteSingleAttachmentById(Long attachmentId);   // 첨부파일 ID로 특정 첨부파일 DB 레코드 삭제
}

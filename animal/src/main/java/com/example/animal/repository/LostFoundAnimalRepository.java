package com.example.animal.repository;

import com.example.animal.entity.AttachmentFile;
import com.example.animal.entity.LostFoundAnimal;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LostFoundAnimalRepository {
    int insert(LostFoundAnimal animal);
    LostFoundAnimal findById(Long id);
    List<LostFoundAnimal> findAll();
    int update(LostFoundAnimal animal);
    int deleteById(Long id);
    void updateLikeCount(Long id); // 추가된 메서드
    int increaseViewCount(Long id);
    int insertAttachment(AttachmentFile file);
    List<AttachmentFile> findAttachmentsByAnimalId(Long animalId);
    void deleteAttachmentsByAnimalId(Long animalId);
    AttachmentFile findAttachmentById(Long attachmentId);
    int deleteSingleAttachmentById(Long attachmentId);
}
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
    void updateLikeCount(Long id); // 추가된 메서드
    int increaseViewCount(Long id);
    int insertAttachment(AttachmentFile file);
    List<AttachmentFile> findAttachmentsByAnimalId(Long animalId);
    void deleteAttachmentsByAnimalId(Long animalId);
    AttachmentFile findAttachmentById(Long attachmentId);
    int deleteSingleAttachmentById(Long attachmentId);

    // --- 새로 추가할 페이징 관련 메서드들 ---

    /**
     * 페이지별 게시글 목록을 조회합니다.
     * @param size 페이지당 게시글 수
     * @param offset 시작 오프셋 (건너뛸 게시글 수)
     * @return LostFoundAnimal 목록
     */
    List<LostFoundAnimal> findByPage(@Param("size") int size, @Param("offset") int offset);
    List<AttachmentFile> findAttachmentsByBoardTypeAndBoardIds(@Param("boardType") String boardType, @Param("boardIds") List<Long> boardIds);

    /**
     * 전체 LostFoundAnimal 게시글의 개수를 조회합니다.
     * @return 전체 게시글 개수
     */
    int countAll();




}
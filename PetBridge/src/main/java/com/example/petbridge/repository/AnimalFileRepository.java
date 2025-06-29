package com.example.petbridge.repository;

import com.example.petbridge.entity.AnimalFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AnimalFileRepository {

    void insert(AnimalFile animalFile);

    List<AnimalFile> findByAnimalId(Long animalId);

    // ★★★ 동물 삭제 시 파일 정보를 한번에 지우기 위한 메소드 선언 추가 ★★★
    void deleteByAnimalId(Long animalId);

    // (이하 다른 메소드들...)
    Optional<AnimalFile> findById(Long id); // 개별 파일 조회

    void deleteById(Long id); // 개별 파일 삭제

    void deleteFilesByPaths(List<String> filePaths);
}
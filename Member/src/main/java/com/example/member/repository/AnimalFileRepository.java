package com.example.member.repository;
import com.example.member.entity.AnimalFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AnimalFileRepository {
    List<AnimalFile> findByAnimalId(Long animalId);
    void insert(AnimalFile animalFile);

    void deleteFilesByPaths(List<String> filePaths);
}
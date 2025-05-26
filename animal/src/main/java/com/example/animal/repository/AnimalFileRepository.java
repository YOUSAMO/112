package com.example.animal.repository;

import com.example.animal.entity.AnimalFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AnimalFileRepository {
    List<AnimalFile> findByAnimalId(Long animalId);
    void insert(AnimalFile animalFile);

    void deleteFilesByPaths(List<String> filePaths);
}
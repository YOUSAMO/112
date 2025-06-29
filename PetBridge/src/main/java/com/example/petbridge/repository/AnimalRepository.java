package com.example.petbridge.repository;
import com.example.petbridge.entity.Animal;
import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AnimalRepository {
    List<Animal> findAll();

    // 페이징용 메서드, Map 대신 명확하게 파라미터 받기 권장
    List<Animal> findAllWithPaging(@Param("offset") int offset, @Param("size") int size);

    int countAll();

    Optional<Animal> findById(Long id);
    void insert(Animal animal);
    void update(Animal animal);
    void deleteById(Long id);

    List<Animal> getAnimalsByCondition(
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("keyword") String keyword,
            @Param("species") String species);

    int getTotalCountByCondition(
            @Param("keyword") String keyword,
            @Param("species") String species);


    List<Animal> findByAnimalIds(@Param("animalIds") List<Long> animalIds);
}

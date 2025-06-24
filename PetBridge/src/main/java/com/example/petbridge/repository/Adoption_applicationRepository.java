package com.example.petbridge.repository;


import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface Adoption_applicationRepository {

 void insert(Adoption_application adoption_application);
 void update(Adoption_application adoption_application);
 void delete(Adoption_application adoption_application);
 int countByUserIdAndAnimalId(@Param("u_id") String u_id, @Param("animal_id") long animal_id);
 List<Adoption_application> findByUserId(@Param("userId") String userId);

}

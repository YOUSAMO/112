package com.example.animal.repository;


import com.example.animal.entity.Adoption_application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface Adoption_applicationRepository {

 void insert(Adoption_application adoption_application);
 void update(Adoption_application adoption_application);
 void delete(Adoption_application adoption_application);
 int countByUserIdAndAnimalId(@Param("u_id") String u_id, @Param("animal_id") long animal_id);

}

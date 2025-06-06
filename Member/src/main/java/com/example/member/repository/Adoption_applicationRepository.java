package com.example.member.repository;


import com.example.member.entity.Adoption_application;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface Adoption_applicationRepository {

 void insert(Adoption_application adoption_application);
 void update(Adoption_application adoption_application);
 void delete(Adoption_application adoption_application);


}

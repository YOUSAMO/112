package com.example.petbridge.repository;

import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VolunteerRepository {

    void insert(Volunteer volunteer);
    List<Volunteer> findByUserId(@Param("u_id") String u_id);
    //void update
    //void delete


}

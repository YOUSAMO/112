package com.example.petbridge.repository;

import com.example.petbridge.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerRepository {

    void insert(Volunteer volunteer);
    //void update
    //void delete


}

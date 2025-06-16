package com.example.animal.repository;

import com.example.animal.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerRepository {

    void insert(Volunteer volunteer);
    //void update
    //void delete


}

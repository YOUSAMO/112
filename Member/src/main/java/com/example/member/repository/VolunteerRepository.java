package com.example.member.repository;

import com.example.member.entity.Volunteer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerRepository {

    void insert(Volunteer volunteer);
    //void update
    //void delete


}

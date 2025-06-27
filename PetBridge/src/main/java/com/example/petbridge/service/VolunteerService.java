package com.example.petbridge.service;


import com.example.petbridge.entity.Volunteer;
import com.example.petbridge.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public void save(Volunteer volunteer) {

         volunteerRepository.insert(volunteer);
    }



    public List<Volunteer> findByUserId(String u_id) {
        return volunteerRepository.findByUserId(u_id);
    }


    public void deleteById(Long id) {
        volunteerRepository.deleteById(id);
    }

}

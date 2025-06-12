package com.example.member.service;


import com.example.member.entity.Volunteer;
import com.example.member.repository.VolunteerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class VolunteerService {

    private final VolunteerRepository volunteerRepository;

    public void save(Volunteer volunteer) {
        volunteerRepository.insert(volunteer);
    }

}

package com.example.petbridge.service;


import com.example.petbridge.entity.Volunteer;
import com.example.petbridge.repository.VolunteerRepository;
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

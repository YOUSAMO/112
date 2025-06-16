package com.example.animal.service;


import com.example.animal.entity.Volunteer;
import com.example.animal.repository.VolunteerRepository;
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

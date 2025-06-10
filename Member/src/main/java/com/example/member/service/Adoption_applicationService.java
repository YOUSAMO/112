package com.example.member.service;


import com.example.member.entity.Adoption_application;
import com.example.member.repository.Adoption_applicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Adoption_applicationService {

    private final Adoption_applicationRepository adoption_applicationRepository;


    public void saveApplication(Adoption_application adoption) {
        adoption_applicationRepository.insert(adoption);
    }





}

package com.example.petbridge.service;


import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.repository.Adoption_applicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Adoption_applicationService {

    private final Adoption_applicationRepository adoption_applicationRepository;


    public void saveApplication(Adoption_application adoption, Long animal_id) {
        adoption.setAnimal_id(animal_id);
        adoption_applicationRepository.insert(adoption);
    }

    public boolean existsApplication(String u_id, Long animal_id) {

        int count = adoption_applicationRepository.countByUserIdAndAnimalId(u_id, animal_id);

        return count > 0;

    }





}

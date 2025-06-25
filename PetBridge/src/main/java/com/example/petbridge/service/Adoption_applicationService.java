package com.example.petbridge.service;

import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.entity.Animal;
import com.example.petbridge.repository.Adoption_applicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Adoption_applicationService {

    private final Adoption_applicationRepository adoptionApplicationRepository;
    private final AnimalService animalService;

    @Transactional
    public void saveApplication(Adoption_application application) throws IOException {
        Long originalAnimalId = application.getAnimal_id();
        Optional<Animal> existingAnimal = animalService.getAnimalById(originalAnimalId);
        Animal savedAnimal;

        if (existingAnimal.isEmpty()) {
            Animal animalFromJson = animalService.findAnimalFromJsonFile(originalAnimalId)
                    .orElseThrow(() -> new IllegalStateException("신청하려는 동물 정보를 찾을 수 없습니다. ID: " + originalAnimalId));

            animalFromJson.setCreated_by(application.getUId());
            savedAnimal = animalService.saveAnimalToDb(animalFromJson);
        } else {
            savedAnimal = existingAnimal.get();
        }

        application.setAnimal_id(savedAnimal.getId());

        // --- [핵심 수정] ---
        // 최종 저장 직전, 신청서 객체에 동물의 이름(품종)을 설정합니다.
        application.setAnimalName(savedAnimal.getName());

        adoptionApplicationRepository.insert(application);
    }

    public boolean existsApplication(String uId, Long animal_id) {
        return adoptionApplicationRepository.countByUserIdAndAnimalId(uId, animal_id) > 0;
    }

    public List<Adoption_application> findByUserId(String userId) {
        return adoptionApplicationRepository.findByUserId(userId);
    }

    public List<Adoption_application> getAllApplications() {
        return adoptionApplicationRepository.findAll();
    }

    public void updateStatus(Long id, String status) {
        adoptionApplicationRepository.updateStatus(id, status);
    }

    public void deleteApplication(Long id) {
        adoptionApplicationRepository.deleteById(id);
    }

    public Adoption_application findById(Long id) {
        return adoptionApplicationRepository.findById(id);
    }





}
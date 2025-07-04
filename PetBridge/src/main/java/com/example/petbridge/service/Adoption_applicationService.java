package com.example.petbridge.service;

import com.example.petbridge.DTO.MyPageApplicationDTO;
import com.example.petbridge.entity.Adoption_application;
import com.example.petbridge.entity.Animal; // Animal DTO를 가져오기 위해 여전히 필요
import com.example.petbridge.repository.Adoption_applicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Adoption_application 저장에 트랜잭션 필요

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Adoption_applicationService {

    private final Adoption_applicationRepository adoptionApplicationRepository;
    private final AnimalService animalService; // JSON 파일에서 동물 정보를 읽어오는 서비스

    @Transactional // Adoption_application 테이블에 데이터를 저장하는 트랜잭션
    public void saveApplication(Adoption_application application) throws IOException {
        // originalAnimalId는 이제 JSON 파일의 DesertionNo (유기번호)를 나타냅니다.
        // 이는 Adoption_application 엔티티의 animal_id 필드에 이미 설정되어 넘어옵니다.
        Long animalJsonId = application.getAnimal_id();

        // 1. animal 테이블에 저장하거나 조회하는 로직은 모두 제거됩니다.
        //    대신, 입양 신청서 필드를 채우기 위해 JSON 파일에서 해당 동물 정보를 조회합니다.
        Optional<Animal> animalFromJSON = animalService.findAnimalFromJsonFile(animalJsonId);

        if (animalFromJSON.isEmpty()) {
            // JSON 파일에서 해당 동물 정보를 찾을 수 없는 경우 예외 발생
            throw new IllegalStateException("입양 신청하려는 동물 정보를 JSON 파일에서 찾을 수 없습니다. ID: " + animalJsonId);
        }

        Animal animal = animalFromJSON.get();

        // 2. Adoption_application 엔티티의 동물 관련 필드들을 JSON에서 가져온 정보로 채웁니다.
        //    (Controller에서 이미 일부 채워져 넘어올 수 있지만, Service에서 최종적으로 보장하는 것이 좋습니다.)
        application.setAnimalName(animal.getName()); // 동물의 이름 (품종 포함)
        application.setAnimalType(animal.getSpecies()); // 동물의 종류 (강아지, 고양이 등)
        // animalDetail 필드에 나이와 성별 정보를 포맷하여 설정
        application.setAnimalDetail(String.format("%d살 / %s", animal.getAge(), animal.getGender()));

        // application.setAnimal_id(animalJsonId); // 이 값은 이미 컨트롤러에서 설정되어 넘어옴

        // 3. Adoption_application 테이블에 입양 신청서를 저장합니다.
        //    이때 animal_id는 JSON의 유기번호 값이 저장됩니다.
        adoptionApplicationRepository.insert(application);
    }

    /**
     * 특정 사용자가 특정 동물(JSON ID 기준)에 대해 이미 입양 신청을 했는지 확인합니다.
     * @param u_id 사용자 ID
     * @param animal_id JSON 파일의 동물 ID (유기번호)
     * @return 이미 신청했으면 true, 아니면 false
     */
    public boolean existsApplication(String u_id, Long animal_id) {
        // Adoption_application 테이블의 animal_id 컬럼에 JSON ID (유기번호)가 저장되므로,
        // 이 로직은 여전히 유효하며 정확하게 중복 신청을 확인합니다.
        return adoptionApplicationRepository.countByUserIdAndAnimalId(u_id, animal_id) > 0;
    }

    /**
     * 특정 사용자의 입양 신청 내역을 조회합니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 입양 신청 목록
     */
    public List<Adoption_application> findByUserId(String userId) {
        // Adoption_applicationRepository의 findByUserId 쿼리가
        // animal 테이블 JOIN 없이 Adoption_application 테이블 자체의 데이터를 가져오도록
        // XML 매퍼에서 수정되어야 합니다.
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


    public List<MyPageApplicationDTO> getMyAdoptionApplicationsWithAnimalAndUser(String userId) {
        return adoptionApplicationRepository.findAdoptionApplicationsWithAnimalAndUser(userId);
    }


}
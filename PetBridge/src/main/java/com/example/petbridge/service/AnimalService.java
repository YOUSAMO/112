package com.example.petbridge.service;

import com.example.petbridge.DTO.AnimalApiDTO;
import com.example.petbridge.entity.Animal;
import com.example.petbridge.repository.AnimalRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Autowired
    public AnimalService(AnimalRepository animalRepository, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.animalRepository = animalRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public List<Animal> findAndConvertFromPublicData() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/data/animals.json");
        InputStream inputStream = resource.getInputStream();
        String jsonResponse = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        Map<String, Object> fullResponse = objectMapper.readValue(jsonResponse, Map.class);
        Map<String, Object> responseBody = (Map<String, Object>) fullResponse.get("response");
        Map<String, Object> bodyMap = (Map<String, Object>) responseBody.get("body");

        if (bodyMap == null || bodyMap.get("items") == null) {
            return Collections.emptyList();
        }

        Map<String, Object> itemsMap = (Map<String, Object>) bodyMap.get("items");
        Object itemObject = itemsMap.get("item");

        List<AnimalApiDTO> dtoList = new ArrayList<>();
        if (itemObject instanceof List) {
            ((List<LinkedHashMap<String, Object>>) itemObject).forEach(item -> dtoList.add(objectMapper.convertValue(item, AnimalApiDTO.class)));
        } else if (itemObject instanceof Map) {
            dtoList.add(objectMapper.convertValue((LinkedHashMap<String, Object>) itemObject, AnimalApiDTO.class));
        }

        return dtoList.stream()
                .map(this::convertSingleDtoToEntity)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Optional<Animal> findAnimalFromJsonFile(Long animalId) throws IOException {
        return findAndConvertFromPublicData().stream()
                .filter(animal -> animalId.equals(animal.getId()))
                .findFirst();
    }

    private Animal convertSingleDtoToEntity(AnimalApiDTO dto) {
        if (dto == null || dto.getDesertionNo() == null || dto.getDesertionNo().trim().isEmpty()) {
            return null;
        }
        Animal animal = new Animal();
        try {
            animal.setId(Long.parseLong(dto.getDesertionNo()));
        } catch (NumberFormatException e) {
            return null;
        }
        animal.setName(dto.getKindName());
        if (dto.getKindName() != null && dto.getKindName().startsWith("[개]")) animal.setSpecies("강아지");
        else if (dto.getKindName() != null && dto.getKindName().startsWith("[고양이]")) animal.setSpecies("고양이");
        else animal.setSpecies("기타");

        try {
            int birthYear = Integer.parseInt(dto.getAge().substring(0, 4));
            animal.setAge(Math.max(1, LocalDate.now().getYear() - birthYear + 1));
        } catch (Exception e) {
            animal.setAge(1);
        }

        if ("M".equalsIgnoreCase(dto.getSexCd())) animal.setGender("수컷");
        else if ("F".equalsIgnoreCase(dto.getSexCd())) animal.setGender("암컷");
        else animal.setGender("미상");

        animal.setNeutered("Y".equalsIgnoreCase(dto.getNeuterYn()));
        animal.setVaccinated(false);
        animal.setDescription(dto.getSpecialMark());

        try {
            animal.setArrivalDate(LocalDate.parse(dto.getHappenDt(), DateTimeFormatter.ofPattern("yyyyMMdd")));
        } catch (Exception e) {
            animal.setArrivalDate(LocalDate.now());
        }

        String imageUrl = dto.getPopfile1() != null ? dto.getPopfile1() : dto.getPopfile();
        animal.setLikes(imageUrl);
        return animal;
    }

    public Optional<Animal> getAnimalById(Long id) {
        return animalRepository.findById(id);
    }

    // --- [최종 수정된 부분] ---
    @Transactional
    public Animal saveAnimalToDb(Animal animal) { // 1. 반환 타입을 void -> Animal 로 변경
        animalRepository.insert(animal);
        return animal; // 2. MyBatis가 id를 채워준 animal 객체를 그대로 반환
    }
}
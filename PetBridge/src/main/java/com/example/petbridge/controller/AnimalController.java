package com.example.petbridge.controller;

import com.example.petbridge.entity.Animal;
import com.example.petbridge.service.AnimalService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/animals")
public class AnimalController {

    private final AnimalService animalService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    @GetMapping({"", "/", "/list"})
    public String getAllAnimals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String species,
            Model model, HttpSession session) {
        try {
            List<Animal> allAnimals = animalService.findAndConvertFromPublicData();

            List<Animal> filteredAnimals = allAnimals.stream()
                    .filter(animal -> species == null || species.isEmpty() || species.equals(animal.getSpecies()))
                    .collect(Collectors.toList());

            int totalCount = filteredAnimals.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalCount);
            List<Animal> paginatedAnimals = (startIndex < totalCount) ? filteredAnimals.subList(startIndex, endIndex) : Collections.emptyList();

            model.addAttribute("animals", paginatedAnimals);
            model.addAttribute("totalCount", totalCount);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", page);
            model.addAttribute("species", species);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "동물 목록을 불러오는 중 오류가 발생했습니다.");
            model.addAttribute("animals", new ArrayList<>());
        }
        model.addAttribute("loggedInUserId", session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY));
        return "animal/animalList";
    }

    @GetMapping("/{id}")
    public String getAnimalById(@PathVariable Long id, Model model) {
        try {
            Optional<Animal> foundAnimal = animalService.findAnimalFromJsonFile(id);
            if (foundAnimal.isPresent()) {
                model.addAttribute("animal", foundAnimal.get());
            } else {
                model.addAttribute("error", "해당 ID의 동물을 찾을 수 없습니다: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "상세 정보를 불러오는 중 오류가 발생했습니다.");
        }
        return "animal/animalDetail";
    }
}
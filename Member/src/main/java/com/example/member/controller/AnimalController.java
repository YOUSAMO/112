package com.example.member.controller;

import com.example.member.entity.Animal;
import com.example.member.entity.AnimalFile;
import com.example.member.service.AnimalFileService;
import com.example.member.service.AnimalService;
import com.example.member.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/animals")
public class AnimalController {

    private final AnimalService animalService;
    private final FileStorageService fileStorageService;
    private final AnimalFileService animalFileService;
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";

    @Autowired
    public AnimalController(AnimalService animalService, FileStorageService fileStorageService, AnimalFileService animalFileService) {
        this.animalService = animalService;
        this.fileStorageService = fileStorageService;
        this.animalFileService = animalFileService;
    }

    @GetMapping({"", "/", "/list"})
    public String getAllAnimals(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String species,
            Model model, HttpSession session) {

        System.out.println("GET /animals 호출됨, page=" + page + ", size=" + size + ", keyword=" + keyword + ", species=" + species);

        int offset = (page - 1) * size;

        List<Animal> animals;
        int totalCount;

        if ((keyword != null && !keyword.isEmpty()) || (species != null && !species.isEmpty())) {
            animals = animalService.getAnimalsByCondition(offset, size, keyword, species);
            totalCount = animalService.getTotalCountByCondition(keyword, species);
        } else {
            animals = animalService.getAnimalsWithPaging(offset, size);
            totalCount = animalService.getTotalAnimalCount();
        }

        Map<Long, List<AnimalFile>> animalFiles = new HashMap<>();
        for (Animal animal : animals) {
            List<AnimalFile> files = animalService.getFilesByAnimalId(animal.getId());
            animalFiles.put(animal.getId(), files);
        }
        model.addAttribute("loggedInUserId", LOGGED_IN_USER_ID_SESSION_KEY);
        model.addAttribute("animals", animals);
        model.addAttribute("animalFiles", animalFiles);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalCount", totalCount);

        int totalPages = (int) Math.ceil((double) totalCount / size);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("keyword", keyword);
        model.addAttribute("species", species);

        return "animal/animalList";
    }

    @GetMapping("/{id}")
    public String getAnimalById(@PathVariable Long id, Model model) {
        Optional<Animal> animal = animalService.getAnimalById(id);
        if (animal.isPresent()) {
            model.addAttribute("animal", animal.get());
            model.addAttribute("files", animalService.getFilesByAnimalId(id));
            return "animal/animalDetail";
        } else {
            model.addAttribute("error", "해당 동물을 찾을 수 없습니다.");
            return "redirect:/animals";
        }
    }
    // [로그인 사용자만 O] 동물 등록
    @GetMapping("/add")
    public String showAddForm(Model model, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        model.addAttribute("animal", new Animal());
        return "animal/animalForm";
    }
    @PostMapping("/add")
    public String addAnimal(@ModelAttribute @Validated Animal animal, BindingResult result,
                            @RequestParam("files") MultipartFile[] files,
                            RedirectAttributes redirectAttributes, Model model, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }

        // [여기서 로그인한 사용자의 u_id를 등록자 정보로 저장]
        String loggedInUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        animal.setCreated_by(loggedInUserId); // ★ 반드시 추가!

        if (result.hasErrors()) {
            return "animal/animalForm";
        }
        try {
            animalService.addAnimal(animal, files);
            redirectAttributes.addFlashAttribute("message", "동물이 추가되었습니다!");
        } catch (Exception e) {
            model.addAttribute("error", "동물 추가 중 오류가 발생했습니다: " + e.getMessage());
            return "animal/animalForm";
        }
        return "redirect:/animals";
    }

    // [로그인 사용자만 O] 동물 수정
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        Optional<Animal> animal = animalService.getAnimalById(id);
        if (animal.isPresent()) {
            model.addAttribute("animal", animal.get());
            model.addAttribute("files", animalService.getFilesByAnimalId(id));
            return "animal/animalForm";
        } else {
            model.addAttribute("error", "해당 동물을 찾을 수 없습니다.");
            return "redirect:/animals";
        }
    }
    @PostMapping("/edit/{id}")
    public String updateAnimal(@PathVariable Long id, @ModelAttribute @Validated Animal animal, BindingResult result,
                               @RequestParam("files") MultipartFile[] files,
                               RedirectAttributes redirectAttributes, Model model, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            model.addAttribute("files", animalService.getFilesByAnimalId(id));
            return "animal/animalForm";
        }
        Optional<Animal> existingAnimalOpt = animalService.getAnimalById(id);
        if (existingAnimalOpt.isPresent()) {
            Animal existingAnimal = existingAnimalOpt.get();
            if (animal.getArrivalDate() == null) {
                animal.setArrivalDate(existingAnimal.getArrivalDate());
            }
        }
        animal.setId(id);
        try {
            animalService.updateAnimal(animal, files);
            redirectAttributes.addFlashAttribute("message", "동물이 수정되었습니다!");
        } catch (Exception e) {
            model.addAttribute("error", "동물 수정 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("files", animalService.getFilesByAnimalId(id));
            return "animal/animalForm";
        }
        return "redirect:/animals";
    }

    // [로그인 사용자만 O] 동물 삭제
    @GetMapping("/delete/{id}")
    public String deleteAnimal(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        try {
            animalService.deleteAnimal(id);
            redirectAttributes.addFlashAttribute("message", "동물이 삭제되었습니다!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "동물 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/animals";
    }
    // [로그인 사용자만 O] 첨부파일 삭제
    @PostMapping("/delete-file")
    public String deleteFiles(@RequestParam(value = "filePaths", required = false) List<String> filePaths,
                              @RequestParam("animalId") Long animalId,
                              RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            return "redirect:/login";
        }
        try {
            if (filePaths != null && !filePaths.isEmpty()) {
                for (String filePath : filePaths) {
                    fileStorageService.deleteFile(filePath);
                }
                animalFileService.deleteFilesByPaths(filePaths);
                redirectAttributes.addFlashAttribute("message", "선택한 파일들이 삭제되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("message", "선택된 파일이 없습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "파일 삭제 중 오류 발생: " + e.getMessage());
        }
        return "redirect:/animals/edit/" + animalId;
    }


    // [로그인 사용자만 O] (추가) 입양 신청
    @PostMapping("/adopt/{id}")
    public String adoptAnimal(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY) == null) {
            redirectAttributes.addFlashAttribute("error", "입양 신청은 로그인 후 이용 가능합니다.");
            return "redirect:/login";
        }
        // TODO: 입양 신청 처리 로직 구현
        redirectAttributes.addFlashAttribute("message", "입양 신청이 완료되었습니다!");
        return "redirect:/animals/" + id;
    }


}

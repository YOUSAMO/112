package com.example.animal.controller;

import com.example.animal.entity.LostFoundAnimal;
import com.example.animal.service.LostFoundAnimalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lostfound")
public class LostFoundAnimalController {

    private final LostFoundAnimalService animalService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final String CONTROLLER_BOARD_TYPE = "lostfound";
    private static final String CONTROLLER_FOLDER_PREFIX = "LostFound_";


    @Autowired
    public LostFoundAnimalController(LostFoundAnimalService animalService) {
        this.animalService = animalService;
    }

    @GetMapping
    public String root() {
        return "redirect:/lostfound/list";
    }

    @GetMapping("/list")
    public String list(Model model, HttpSession session) {
        List<LostFoundAnimal> list = animalService.getList();
        String currentUserId = (String) session.getAttribute("loggedInUserId");

        model.addAttribute("list", list);
        model.addAttribute("currentUserId", currentUserId);

        // ### 최종 경로 수정: 사용자 파일 이름과 위치에 정확히 맞춤 ###
        return "lostfound/lostfoundList";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("animal", new LostFoundAnimal());
        model.addAttribute("isNew", true);
        // ### 최종 경로 수정: 사용자 파일 이름과 위치에 정확히 맞춤 ###
        return "lostfound/lostfoundForm";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute LostFoundAnimal animal,
                           @RequestParam("files") List<MultipartFile> files,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) throws IOException {

        String userId = (String) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return "redirect:/login";
        }

        animalService.register(animal, files, userId);
        redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 등록되었습니다.");
        return "redirect:/lostfound/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUserId");
        LostFoundAnimal animal = animalService.get(id, userId);

        if (animal == null) {
            return "redirect:/lostfound/list";
        }
        animalService.increaseViewCount(id);
        model.addAttribute("animal", animal);
        // ### 최종 경로 수정: 사용자 파일 이름과 위치에 정확히 맞춤 ###
        return "lostfound/lostfoundView";
    }

    @GetMapping("/modify/{id}")
    public String modifyForm(@PathVariable Long id, Model model) {
        LostFoundAnimal animal = animalService.get(id, null);
        if (animal == null) {
            return "redirect:/lostfound/list";
        }
        model.addAttribute("animal", animal);
        model.addAttribute("isNew", false);
        // ### 최종 경로 수정: 사용자 파일 이름과 위치에 정확히 맞춤 ###
        return "lostfound/lostfoundForm";
    }

    @PostMapping("/modify")
    public String modify(@ModelAttribute LostFoundAnimal animal,
                         @RequestParam("files") List<MultipartFile> files,
                         RedirectAttributes redirectAttributes) throws IOException {
        animalService.modify(animal, files);
        redirectAttributes.addFlashAttribute("message", "게시글이 성공적으로 수정되었습니다.");
        return "redirect:/lostfound/detail/" + animal.getId();
    }

    @PostMapping("/remove/{id}")
    public String remove(@PathVariable Long id) {
        animalService.remove(id);
        return "redirect:/lostfound/list";
    }

    @PostMapping("/like/{id}")
    @ResponseBody
    public ResponseEntity<?> like(@PathVariable("id") Long boardId, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUserId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            Map<String, Object> result = animalService.toggleLike(userId, boardId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("오류가 발생했습니다.");
        }
    }

    @DeleteMapping("/attachment/{attachmentId}")
    @ResponseBody
    public ResponseEntity<String> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            boolean deleted = animalService.deleteSingleAttachment(attachmentId);
            if (deleted) {
                return ResponseEntity.ok("첨부파일이 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("첨부파일을 찾을 수 없거나 삭제에 실패했습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("첨부파일 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/uploads/{boardType}/{boardId}/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String boardType,
                                              @PathVariable String boardId,
                                              @PathVariable String fileName) {
        try {
            if (!CONTROLLER_BOARD_TYPE.equals(boardType)) {
                return ResponseEntity.badRequest().build();
            }
            String actualFolderNameInPath = CONTROLLER_FOLDER_PREFIX + boardId;
            Path fileSystemPath = Paths.get(uploadDir, boardType, actualFolderNameInPath, fileName);
            Resource resource = new UrlResource(fileSystemPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(fileSystemPath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
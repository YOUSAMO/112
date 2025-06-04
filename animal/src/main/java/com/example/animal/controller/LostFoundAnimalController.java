package com.example.animal.controller;

import com.example.animal.entity.LostFoundAnimal;
import com.example.animal.service.LostFoundAnimalService;
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

import java.io.IOException; // remove 메소드에서만 사용될 수 있음 (현재는 아님)
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/lostfound")
public class LostFoundAnimalController {

    private final LostFoundAnimalService animalService;

    @Value("${file.upload.dir}")
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
    public String list(Model model) {
        List<LostFoundAnimal> list = animalService.getList();
        model.addAttribute("list", list);
        return "lostfound/lostfoundlist";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("animal")) {
            model.addAttribute("animal", new LostFoundAnimal());
        }
        return "lostfound/lostfoundregister";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute LostFoundAnimal animal,
                           @RequestParam("files") List<MultipartFile> files) throws IOException {
        animalService.register(animal, files);
        return "redirect:/lostfound/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        LostFoundAnimal animal = animalService.get(id);
        if (animal == null) {
            return "redirect:/lostfound/list";
        }
        animalService.increaseViewCount(id);
        model.addAttribute("animal", animal);
        return "lostfound/lostfounddetail";
    }

    @GetMapping("/modify/{id}")
    public String modifyForm(@PathVariable Long id, Model model) {
        LostFoundAnimal animal = animalService.get(id);
        if (animal == null) {
            return "redirect:/lostfound/list";
        }
        model.addAttribute("animal", animal);
        return "lostfound/lostfoundmodify";
    }

    @PostMapping("/modify")
    public String modify(@ModelAttribute LostFoundAnimal animal,
                         @RequestParam("files") List<MultipartFile> files) throws IOException {
        animalService.modify(animal, files);
        return "redirect:/lostfound/detail/" + animal.getId();
    }

    @PostMapping("/remove/{id}")
    public String remove(@PathVariable Long id) {
        animalService.remove(id);
        return "redirect:/lostfound/list";
    }

    @PostMapping("/like/{id}")
    @ResponseBody
    public String like(@PathVariable Long id) {
        animalService.increaseLikeCount(id);
        return "success";
    }

    @DeleteMapping("/attachment/{attachmentId}")
    @ResponseBody
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            boolean deleted = animalService.deleteSingleAttachment(attachmentId);
            if (deleted) {
                return ResponseEntity.ok().body("첨부파일이 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("첨부파일을 삭제하지 못했습니다. (파일을 찾을 수 없거나 삭제 중 오류)");
            }
        } catch (Exception e) {
            System.err.println("첨부파일 삭제 중 서버 오류 발생: attachmentId=" + attachmentId + ", error=" + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("첨부파일 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @GetMapping("/uploads/{boardType}/{boardId}/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String boardType,
                                              @PathVariable String boardId,
                                              @PathVariable String fileName) {
        Path fileSystemPath = null;
        try {
            if (CONTROLLER_BOARD_TYPE.equals(boardType)) {
                String actualFolderNameInPath = CONTROLLER_FOLDER_PREFIX + boardId;
                fileSystemPath = Paths.get(uploadDir, boardType, actualFolderNameInPath, fileName);
            } else {
                System.err.println("LostFoundAnimalController: Unsupported boardType for file serving: " + boardType);
                return ResponseEntity.notFound().build(); // ResponseEntity<Void> 반환 (호환 가능)
            }

            System.out.println("LostFoundAnimalController: Attempting to serve file from: " + fileSystemPath.toString());
            Resource resource = new UrlResource(fileSystemPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(fileSystemPath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource); // ResponseEntity<Resource> 반환
            } else {
                System.err.println("LostFoundAnimalController: File not found or not readable: " + fileSystemPath.toString());
                return ResponseEntity.notFound().build(); // ResponseEntity<Void> 반환 (호환 가능)
            }
        } catch (Exception e) {
            String pathForError = (fileSystemPath != null) ? fileSystemPath.toString() : "unknown path due to early error or unsupported boardType";
            System.err.println("LostFoundAnimalController: Error serving file: " + pathForError + " - " + e.getMessage());
            e.printStackTrace();
            // ★★★ 수정된 부분 ★★★
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // ResponseEntity<Void> 반환 (호환 가능)
            // 또는 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<Resource>body(null); 와 같이 명시적 null body
        }
    }
}
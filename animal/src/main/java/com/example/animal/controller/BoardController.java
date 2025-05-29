package com.example.animal.controller;

import com.example.animal.entity.Board;
import com.example.animal.service.BoardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*; // @PostMapping 사용을 위해 유지
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    @Autowired
    private BoardService boardService;

    // === 게시글 관련 CRUD ===
    @GetMapping("/boards")
    public String listBoards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        List<Board> boards = boardService.getBoardsByPage(page, size);
        int totalCount = boardService.getTotalBoardCount();

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        int totalPages = (int) Math.ceil((double) totalCount / size);
        PageInfo pageInfo = new PageInfo(totalPages, page);
        model.addAttribute("pageInfo", pageInfo);

        return "boards/boardList";
    }

    @GetMapping("/boards/new")
    public String showCreateForm(Model model) {
        model.addAttribute("board", new Board());
        return "boards/boardForm";
    }

    @PostMapping("/boards")
    public String createBoard(@ModelAttribute Board board,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files,
                              RedirectAttributes redirectAttributes) {
        try {
            boardService.createBoard(board, files);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 등록되었습니다.");
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생 (게시글 생성)", e);
            redirectAttributes.addFlashAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다.");
            return "redirect:/boards/new";
        } catch (Exception e) {
            log.error("게시글 등록 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다.");
            return "redirect:/boards/new";
        }
        return "redirect:/boards";
    }

    @GetMapping("/boards/{bNo}")
    public String viewBoard(@PathVariable Long bNo, Model model) {
        boardService.increaseViewCount(bNo);
        Board board = boardService.getBoardById(bNo);
        if (board == null) {
            return "redirect:/boards";
        }
        model.addAttribute("board", board);
        return "boards/boardView";
    }

    @GetMapping("/boards/{bNo}/edit")
    public String showEditForm(@PathVariable Long bNo, Model model) {
        Board board = boardService.getBoardById(bNo);
        if (board == null) {
            return "redirect:/boards";
        }
        model.addAttribute("board", board);
        return "boards/boardForm";
    }

    @PostMapping("/boards/{bNo}/edit")
    public String updateBoard(@PathVariable Long bNo,
                              @ModelAttribute Board boardDetailsFromForm,
                              @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                              RedirectAttributes redirectAttributes) {
        try {
            boardService.updateBoard(bNo, boardDetailsFromForm, newFiles);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");
            return "redirect:/boards/" + bNo;
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생 (게시글 {} 수정)", bNo, e);
            redirectAttributes.addFlashAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/boards/" + bNo + "/edit";
        } catch (RuntimeException e) {
            log.error("게시글 수정 중 오류 발생 (게시글 {})", bNo, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/boards/" + bNo + "/edit";
        } catch (Exception e) {
            log.error("예상치 못한 오류 발생 (게시글 {} 수정)", bNo, e);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 예상치 못한 오류가 발생했습니다.");
            return "redirect:/boards/" + bNo + "/edit";
        }
    }

    @PostMapping("/boards/{bNo}/delete")
    public String deleteBoard(@PathVariable Long bNo, RedirectAttributes redirectAttributes) {
        try {
            boardService.deleteBoard(bNo);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("게시글 {} 삭제 중 오류 발생", bNo, e);
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/boards";
    }

    @PostMapping("/boards/{bNo}/like")
    @ResponseBody
    public Map<String, Object> likeBoard(@PathVariable Long bNo) {
        int likeCount = boardService.increaseLikeCount(bNo);
        return Map.of("success", true, "likeCount", likeCount);
    }

    // === 개별 첨부파일 삭제를 위한 컨트롤러 메소드 ===
    // HiddenHttpMethodFilter를 사용하지 않으므로 @PostMapping으로 변경합니다.
    @PostMapping("/attachments/{attachmentId}/delete")
    public String deleteAttachment(@PathVariable Long attachmentId,
                                   @RequestParam Long boardNo, // HTML 폼의 hidden input으로 전송됨
                                   RedirectAttributes redirectAttributes) {
        log.info("첨부파일 삭제 요청 수신 (POST): attachmentId={}, boardNo={}", attachmentId, boardNo);
        try {
            boardService.deleteAttachment(attachmentId);
            redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("첨부파일 {} 삭제 처리 중 오류 발생 (게시글 ID: {})", attachmentId, boardNo, e);
            redirectAttributes.addFlashAttribute("errorMessage", "첨부파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "redirect:/boards/" + boardNo + "/edit"; // 해당 게시글 수정 페이지로 리다이렉트
    }


    // 페이지 정보 내부 클래스
    public static class PageInfo {
        private int totalPages;
        private int currentPage;

        public PageInfo(int totalPages, int currentPage) {
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
        public int getNumber() { return currentPage - 1; }
        public boolean isFirst() { return currentPage == 1; }
        public boolean isLast() { return currentPage == totalPages; }
    }
}
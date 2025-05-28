package com.example.animal.controller;

import com.example.animal.entity.Board;
import com.example.animal.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;

    // 게시글 목록 (페이징 적용)
    @GetMapping
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

    // 게시글 작성 폼 보여주기
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("board", new Board());
        return "boards/boardForm";
    }

    // 게시글 생성 처리
    @PostMapping
    public String createBoard(@ModelAttribute Board board,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files) throws IOException {

        boardService.createBoard(board, files);
        return "redirect:/boards";
    }

    // 게시글 상세 보기
    @GetMapping("/{bNo}")
    public String viewBoard(@PathVariable Long bNo, Model model) {
        boardService.increaseViewCount(bNo);
        Board board = boardService.getBoardById(bNo);
        model.addAttribute("board", board);
        return "boards/boardView";
    }

    // 게시글 수정 폼 보여주기
    @GetMapping("/{bNo}/edit")
    public String showEditForm(@PathVariable Long bNo, Model model) {
        Board board = boardService.getBoardById(bNo);
        model.addAttribute("board", board);
        return "boards/boardForm";
    }

    // 게시글 수정 처리 (첨부파일 MultipartFile 리스트 받음)
    @PostMapping("/{bNo}/edit")
    public String updateBoard(@PathVariable Long bNo,
                              @ModelAttribute Board board,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files) throws IOException {

        board.setBNo(bNo);
        boardService.updateBoard(board, files);
        return "redirect:/boards/" + bNo;
    }

    // 게시글 삭제 처리
    @PostMapping("/{bNo}/delete")
    public String deleteBoard(@PathVariable Long bNo) {
        boardService.deleteBoard(bNo);
        return "redirect:/boards";
    }

    // 좋아요 처리 (Ajax 등 비동기용)
    @PostMapping("/{bNo}/like")
    @ResponseBody
    public Map<String, Object> likeBoard(@PathVariable Long bNo) {
        int likeCount = boardService.increaseLikeCount(bNo);
        return Map.of("likeCount", likeCount);
    }

    // 페이지 정보 클래스
    public static class PageInfo {
        private int totalPages;
        private int currentPage;

        public PageInfo(int totalPages, int currentPage) {
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getNumber() {
            return currentPage - 1;
        }

        public boolean isFirst() {
            return currentPage == 1;
        }

        public boolean isLast() {
            return currentPage == totalPages;
        }
    }
}

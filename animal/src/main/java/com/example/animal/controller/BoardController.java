package com.example.animal.controller;

import com.example.animal.entity.Board;
import com.example.animal.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/boards")
public class BoardController {

    @Autowired
    private BoardService boardService;

    // 게시글 목록 (페이징 적용)
    @GetMapping
    public String listBoards(
            @RequestParam(defaultValue = "1") int page,  // 1부터 시작
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // 게시글 목록 조회 (서비스에서 offset 계산 필요)
        List<Board> boards = boardService.getBoardsByPage(page, size);
        int totalCount = boardService.getTotalBoardCount();

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // 전체 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // pageInfo 객체 생성 (템플릿에서 페이징 관련 정보를 활용하기 위함)
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
    public String createBoard(@ModelAttribute Board board) {
        boardService.createBoard(board);
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

    // 게시글 수정 처리
    @PostMapping("/{bNo}/edit")
    public String updateBoard(@PathVariable Long bNo, @ModelAttribute Board board) {
        board.setBNo(bNo);
        boardService.updateBoard(board);
        return "redirect:/boards/" + bNo;
    }

    // 게시글 삭제 처리
    @PostMapping("/{bNo}/delete")
    public String deleteBoard(@PathVariable Long bNo) {
        boardService.deleteBoard(bNo);
        return "redirect:/boards";
    }


    // PageInfo 클래스: 페이징에 필요한 정보 전달용
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

        // 아래 메서드들은 템플릿에서 Spring Data Page 객체처럼 쓰도록 돕습니다.

        /** 0-based 페이지 번호 */
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

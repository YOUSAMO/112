package com.example.animal.controller;

import com.example.animal.entity.Board;
import com.example.animal.entity.Comment; // Comment 엔티티 임포트
import com.example.animal.service.BoardService;
import com.example.animal.service.CommentService; // CommentService 임포트
import com.example.animal.service.LikeService;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;
    private final CommentService commentService; // CommentService 필드 추가

    // 세션에서 로그인한 사용자 ID를 가져오는 키
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    // 이 컨트롤러에서 다루는 게시판의 기본 타입을 'board'로 가정
    public static final String DEFAULT_BOARD_TYPE = "board";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로

    @Autowired
    public BoardController(BoardService boardService, LikeService likeService, CommentService commentService) { // 생성자 수정
        this.boardService = boardService;
        this.likeService = likeService;
        this.commentService = commentService; // CommentService 주입
    }

    @GetMapping("/boards")
    public String listBoards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model,
            HttpSession session) {
        List<Board> boards = boardService.getBoardsByPage(page, size);
        int totalCount = boardService.getTotalBoardCount();

        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        Map<Long, Map<String, Object>> likeStatuses = new HashMap<>();
        if (boards != null) {
            for (Board board : boards) {
                if (board != null && board.getBNo() != null) {
                    likeStatuses.put(board.getBNo(), likeService.getLikeStatus(currentUserId, board.getBNo(), DEFAULT_BOARD_TYPE));
                }
            }
        }

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("pageInfo", new PageInfo((int) Math.ceil((double) totalCount / size), page));
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatuses", likeStatuses);
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE);


        return "boards/boardList";
    }

    @GetMapping("/boards/new")
    public String showCreateForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }
        model.addAttribute("board", new Board());
        return "boards/boardForm";
    }

    @PostMapping("/boards")
    public String createBoard(@ModelAttribute Board board,
                              @RequestParam(value = "files", required = false) List<MultipartFile> files,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다. 다시 로그인해주세요.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        try {
            boardService.createBoard(board, files, loggedInUserUid);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 등록되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다.");
            return "redirect:/boards/new";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 등록 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/boards/new";
        }
        return "redirect:/boards";
    }

    @GetMapping("/boards/{bNo}")
    public String viewBoard(@PathVariable Long bNo, Model model, HttpSession session) {
        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);

        boardService.increaseViewCount(bNo);
        Board board = boardService.getBoardById(bNo);
        if (board == null) {
            return "redirect:/boards?error=notfound";
        }

        Map<String, Object> likeStatus = likeService.getLikeStatus(currentUserId, bNo, DEFAULT_BOARD_TYPE);

        // 해당 게시글(bNo)에 달린 댓글 목록을 불러와 Model에 추가합니다.
        List<Comment> comments = commentService.getCommentsByPostId(bNo);
        model.addAttribute("comments", comments); // Thymeleaf 뷰에서 'comments'로 접근 가능

        model.addAttribute("board", board);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE);

        System.out.println("### BoardController -> 뷰로 전달 직전 currentUserId: " + currentUserId);

        return "boards/boardView"; // 이 뷰 파일에서 댓글 목록을 표시할 것입니다.
    }

    @GetMapping("/boards/{bNo}/edit")
    public String showEditForm(@PathVariable Long bNo, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        Board boardToEdit = boardService.getBoardById(bNo);
        if (boardToEdit == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "게시글을 찾을 수 없습니다.");
            return "redirect:/boards";
        }

        if (!loggedInUserUid.equals(boardToEdit.getAuthorUid())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 게시글을 수정할 권한이 없습니다.");
            return "redirect:/boards/" + bNo;
        }

        model.addAttribute("board", boardToEdit);
        return "boards/boardForm";
    }

    @PostMapping("/boards/{bNo}/edit")
    public String updateBoard(@PathVariable Long bNo,
                              @ModelAttribute Board boardDetailsFromForm,
                              @RequestParam(value = "files", required = false) List<MultipartFile> newFiles,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다. 다시 로그인해주세요.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        try {
            boardService.updateBoard(bNo, boardDetailsFromForm, newFiles, loggedInUserUid);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 수정되었습니다.");
            return "redirect:/boards/" + bNo;
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/boards/" + bNo + "/edit";
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/boards/" + bNo + "/edit";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 수정 중 예상치 못한 오류가 발생했습니다.");
            return "redirect:/boards/" + bNo + "/edit";
        }
    }

    @PostMapping("/boards/{bNo}/delete")
    public String deleteBoard(@PathVariable Long bNo, RedirectAttributes redirectAttributes,
                              HttpSession session) {
        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        if (loggedInUserUid == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다. 다시 로그인해주세요.");
            return "redirect:" + LOGIN_PAGE_URL;
        }

        try {
            boardService.deleteBoard(bNo, loggedInUserUid);
            redirectAttributes.addFlashAttribute("successMessage", "게시글이 성공적으로 삭제되었습니다.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            Board board = boardService.getBoardById(bNo);
            if(board != null && !loggedInUserUid.equals(board.getAuthorUid())){
                return "redirect:/boards/" + bNo;
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "게시글 삭제 중 오류가 발생했습니다.");
        }
        return "redirect:/boards";
    }

    // PageInfo 내부 클래스 (이전과 동일)
    public static class PageInfo {
        private int totalPages;
        private int currentPage;
        public PageInfo(int totalPages, int currentPage) {
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }
        public int getTotalPages() { return totalPages; }
        public int getCurrentPage() { return currentPage; }
    }
}
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

    // ★★★ 이 부분을 현재 이 컨트롤러가 담당하는 게시판 타입으로 명확하게 지정하세요. ★★★
    // 예: "freeboard", "notice", "qna" 등.
    // 만약 여러 게시판이 있다면, 각 게시판의 컨트롤러에서 해당 타입으로 변경해야 합니다.
    public static final String DEFAULT_BOARD_TYPE = "board"; // 현재 이 컨트롤러가 일반 '자유게시판'을 다룬다고 가정

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
                    // 게시글 목록에서는 좋아요 상태를 가져올 때도 해당 게시판 타입을 넘겨줍니다.
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
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE); // 게시판 목록에도 defaultBoardType 전달

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
        // 게시글 작성 폼에도 기본 게시판 타입을 전달할 필요가 있다면 추가
        // model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE);
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
            // board 객체에 boardType을 설정해야 할 수도 있습니다.
            // board.setBoardType(DEFAULT_BOARD_TYPE); // Board 엔티티에 boardType 필드가 있다면
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

        // 좋아요 상태를 가져올 때도 해당 게시판 타입을 넘겨줍니다.
        Map<String, Object> likeStatus = likeService.getLikeStatus(currentUserId, bNo, DEFAULT_BOARD_TYPE);

        // ★★★ 여기를 수정합니다: 댓글 목록을 불러올 때 게시판 타입도 함께 넘겨줍니다. ★★★
        List<Comment> comments = commentService.getCommentsByPostId(bNo, DEFAULT_BOARD_TYPE); // boardType 전달
        model.addAttribute("comments", comments); // Thymeleaf 뷰에서 'comments'로 접근 가능

        model.addAttribute("board", board);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE); // 현재 게시판 타입을 프론트엔드로 전달 (JS에서 사용)

        System.out.println("### BoardController -> 뷰로 전달 직전 currentUserId: " + currentUserId);
        System.out.println("### BoardController -> 뷰로 전달 직전 defaultBoardType: " + DEFAULT_BOARD_TYPE);

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

        // 게시글 수정 권한 확인 시, 게시글의 boardType도 필요하다면 로직에 포함
        // if (!loggedInUserUid.equals(boardToEdit.getAuthorUid()) || !DEFAULT_BOARD_TYPE.equals(boardToEdit.getBoardType())) { ... }
        if (!loggedInUserUid.equals(boardToEdit.getAuthorUid())) {
            redirectAttributes.addFlashAttribute("errorMessage", "이 게시글을 수정할 권한이 없습니다.");
            return "redirect:/boards/" + bNo;
        }

        model.addAttribute("board", boardToEdit);
        // 게시글 수정 폼에도 기본 게시판 타입을 전달할 필요가 있다면 추가
        // model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE);
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
            // boardDetailsFromForm 에 boardType을 설정해야 할 수도 있습니다.
            // boardDetailsFromForm.setBoardType(DEFAULT_BOARD_TYPE); // Board 엔티티에 boardType 필드가 있다면
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
            // 게시글 삭제 권한 확인 시, 게시글의 boardType도 필요하다면 로직에 포함
            // Board boardToDelete = boardService.getBoardById(bNo);
            // if (!loggedInUserUid.equals(boardToDelete.getAuthorUid()) || !DEFAULT_BOARD_TYPE.equals(boardToDelete.getBoardType())) { ... }
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
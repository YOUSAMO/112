package com.example.member.controller;

import com.example.member.entity.Board;
import com.example.member.service.BoardService;
import com.example.member.service.LikeService; // LikeService import 추가

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap; // HashMap import 추가
import java.util.List;
import java.util.Map;

@Controller
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService; // LikeService 주입

    // 세션에서 로그인한 사용자 ID를 가져오는 키
    private static final String LOGGED_IN_USER_ID_SESSION_KEY = "loggedInUserId";
    // 이 컨트롤러에서 다루는 게시판의 기본 타입을 'board'로 가정 (실제 값으로 변경 가능)
    public static final String DEFAULT_BOARD_TYPE = "board";
    private static final String LOGIN_PAGE_URL = "/login"; // 실제 로그인 페이지 경로

    @Autowired
    public BoardController(BoardService boardService, LikeService likeService) {
        this.boardService = boardService;
        this.likeService = likeService;
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

        // 각 게시물의 좋아요 상태를 담을 Map
        Map<Long, Map<String, Object>> likeStatuses = new HashMap<>();
        if (boards != null) {
            for (Board board : boards) {
                if (board != null && board.getBNo() != null) {
                    // DEFAULT_BOARD_TYPE 사용
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
        model.addAttribute("likeStatuses", likeStatuses); // ★★★ 좋아요 상태들 모델에 추가 ★★★
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE); // ★★★ 뷰에서 boardType 사용 위해 추가 ★★★


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
        boardService.increaseViewCount(bNo);
        Board board = boardService.getBoardById(bNo);
        if (board == null) {
            // 게시글이 없는 경우에 대한 처리 (예: 404 페이지 또는 목록으로 리다이렉트)
            return "redirect:/boards?error=notfound";
        }

        String currentUserId = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
        Map<String, Object> likeStatus = likeService.getLikeStatus(currentUserId, bNo, DEFAULT_BOARD_TYPE);

        model.addAttribute("board", board);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("likeStatus", likeStatus); // ★★★ 좋아요 상태 모델에 추가 ★★★
        model.addAttribute("defaultBoardType", DEFAULT_BOARD_TYPE); // ★★★ 뷰에서 boardType 사용 위해 추가 ★★★

        return "boards/boardView";
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
        } catch (RuntimeException e) { // 서비스에서 권한 문제 등으로 RuntimeException 발생 시
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // 권한 없는 경우, 상세 페이지로 리다이렉트하여 메시지 표시
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

    /*
     * @PostMapping("/boards/{bNo}/like")
     * @ResponseBody
     * public Map<String, Object> likeBoard(@PathVariable Long bNo, HttpSession session) {
     * // 이 로직은 LikeController로 이동하거나, 여기서 직접 LikeService를 호출할 수 있습니다.
     * // LikeController를 별도로 사용한다고 가정하고 여기서는 주석 처리 또는 삭제합니다.
     * // 만약 이 컨트롤러에서 처리하려면:
     * String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
     * if (loggedInUserUid == null) {
     * return Map.of("success", false, "message", "로그인이 필요합니다.");
     * }
     * try {
     * Map<String, Object> result = likeService.toggleLike(loggedInUserUid, bNo, DEFAULT_BOARD_TYPE);
     * result.put("success", true);
     * return result;
     * } catch (Exception e) {
     * return Map.of("success", false, "message", "오류 발생: " + e.getMessage());
     * }
     * }
     */


//    // 첨부파일 삭제 (이전과 동일, 필요시 권한 확인 로직 추가)
//    @PostMapping("/attachments/{attachmentId}/delete")
//    public String deleteAttachment(@PathVariable Long attachmentId,
//                                   @RequestParam Long boardNo, // boardNo는 리다이렉트에 사용
//                                   RedirectAttributes redirectAttributes,
//                                   HttpSession session) {
//        String loggedInUserUid = (String) session.getAttribute(LOGGED_IN_USER_ID_SESSION_KEY);
//        if (loggedInUserUid == null) {
//            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
//            return "redirect:" + LOGIN_PAGE_URL;
//        }
//
//        // TODO: 첨부파일 삭제에 대한 권한 확인 로직 추가 고려
//        // (예: 해당 첨부파일이 속한 게시물의 작성자인지 확인)
//        // Board board = boardService.getBoardByAttachmentId(attachmentId); // 이런 메소드가 필요할 수 있음
//        // if (board == null || !loggedInUserUid.equals(board.getAuthorUid())) {
//        //     redirectAttributes.addFlashAttribute("errorMessage", "첨부파일을 삭제할 권한이 없습니다.");
//        //     return "redirect:/boards/" + boardNo + "/edit";
//        // }
//
//        try {
//            boardService.deleteAttachment(attachmentId);
//            redirectAttributes.addFlashAttribute("successMessage", "첨부파일이 성공적으로 삭제되었습니다.");
//        } catch (RuntimeException e) {
//            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
//        } catch (Exception e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("errorMessage", "첨부파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
//        }
//        return "redirect:/boards/" + boardNo + "/edit";
//    }

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
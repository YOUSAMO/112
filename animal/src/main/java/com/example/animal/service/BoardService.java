package com.example.animal.service;

import com.example.animal.entity.Board;
import com.example.animal.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    public List<Board> getBoardsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return boardRepository.findBoardsByPage(size, offset);
    }

    public int getTotalBoardCount() {
        return boardRepository.countBoards();
    }

    public Board getBoardById(Long bNo) {
        return boardRepository.findById(bNo);
    }

    public void createBoard(Board board) {
        boardRepository.insertBoard(board);
    }

    public void updateBoard(Board board) {
        boardRepository.updateBoard(board);
    }

    public void deleteBoard(Long bNo) {
        boardRepository.deleteBoard(bNo);
    }

    public void increaseViewCount(Long bNo) {
        boardRepository.incrementViewCount(bNo);
    }
}

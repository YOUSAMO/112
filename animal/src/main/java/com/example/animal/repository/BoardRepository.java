package com.example.animal.repository;

import com.example.animal.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BoardRepository {

    void insertBoard(Board board);

    List<Board> findAll();

    // 페이징 쿼리
    List<Board> findBoardsByPage(@Param("limit") int limit, @Param("offset") int offset);

    int countBoards();

    Board findById(Long bNo);

    void updateBoard(Board board);

    void deleteBoard(Long bNo);

    void incrementViewCount(Long bNo);
}

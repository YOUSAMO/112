package com.example.animal.repository;

import com.example.animal.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardRepository {

    void insertBoard(Board board);

    List<Board> findAll();

    List<Board> findBoardsByPage(@Param("limit") int limit, @Param("offset") int offset);

    int countBoards();

    Board findById(Long bNo);

    void updateBoard(Board board);

    void deleteBoard(Long bNo);    // deleteById 대신 deleteBoard로 변경 (XML과 맞춤)

    void incrementViewCount(Long bNo);

    void increaseLikeCount(Long bNo);

    int getLikeCount(Long bNo);

}

package com.example.member.repository;

import com.example.member.entity.Board;
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

    int updateBoard(Board board);

    void deleteBoard(Long bNo);    // deleteById 대신 deleteBoard로 변경 (XML과 맞춤)

    void incrementViewCount(Long bNo);

    void increaseLikeCount(Long bNo);

    int getLikeCount(Long bNo);


    void incrementBoardLikeCount(Long bNo); // board_id 대신 bNo 사용

    void decrementBoardLikeCount(Long bNo); // board_id 대신 bNo 사용

}
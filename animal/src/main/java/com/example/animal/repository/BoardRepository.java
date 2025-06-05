package com.example.animal.repository;

import com.example.animal.entity.Board;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardRepository {

    void insertBoard(Board board); // Board 객체의 authorUid 필드가 사용됨

    List<Board> findAll(); // 반환되는 Board 객체에 authorName이 채워짐

    List<Board> findBoardsByPage(@Param("limit") int limit, @Param("offset") int offset); // 동일

    int countBoards();

    Board findById(Long bNo); // 반환되는 Board 객체에 authorName, authorUid가 채워짐

    int updateBoard(Board board); // Board 객체의 bTitle, bContent가 사용됨

    void deleteBoard(Long bNo);

    void incrementViewCount(Long bNo);

    void increaseLikeCount(Long bNo);

    int getLikeCount(Long bNo);

    void incrementBoardLikeCount(Long bNo); // board_id 대신 bNo 사용
    void decrementBoardLikeCount(Long bNo); // board_id 대신 bNo 사용
}
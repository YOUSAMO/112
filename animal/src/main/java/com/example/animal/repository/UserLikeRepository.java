package com.example.animal.repository;

import com.example.animal.entity.UserLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserLikeRepository {
    int insertLike(UserLike userLike);

    int deleteLike(@Param("userId") String userId,
                   @Param("boardId") Long boardId,
                   @Param("boardType") String boardType);

    UserLike findLike(@Param("userId") String userId,
                      @Param("boardId") Long boardId,
                      @Param("boardType") String boardType);

    // 이 메소드는 board 테이블의 like_count를 직접 사용하므로 필수는 아닐 수 있습니다.
    // int countLikesByBoard(@Param("boardId") Long boardId,
    //                       @Param("boardType") String boardType);
}
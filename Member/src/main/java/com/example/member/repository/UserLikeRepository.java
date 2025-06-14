package com.example.member.repository;

import com.example.member.entity.UserLike;
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

    int deleteLikesByContent(@Param("boardId") Long boardId, @Param("boardType") String boardType);


    // int countLikesByBoard(@Param("boardId") Long boardId,
    //                       @Param("boardType") String boardType);
}
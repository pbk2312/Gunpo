package com.example.gunpo.repository;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 게시물 ID로 조회수 조회
    @Query("SELECT b.viewCount FROM Board b WHERE b.id = :boardId")
    Optional<Integer> findViewCountByBoardId(@Param("boardId") Long boardId);

    // 게시물 ID로 좋아요 수 조회
    @Query("SELECT b.likeCount FROM Board b WHERE b.id = :boardId")
    Optional<Integer> findLikeCountByBoardId(@Param("boardId") Long boardId);

    List<Board> getBoardByAuthor(Member member);


}

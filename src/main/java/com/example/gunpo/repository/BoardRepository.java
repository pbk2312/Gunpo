package com.example.gunpo.repository;

import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 게시물 ID로 조회수 조회
    @Query("SELECT b.viewCount FROM Board b WHERE b.id = :boardId")
    Optional<Integer> findViewCountByBoardId(@Param("boardId") Long boardId);

    // 게시물 ID로 좋아요 수 조회
    @Query("SELECT b.likeCount FROM Board b WHERE b.id = :boardId")
    Optional<Integer> findLikeCountByBoardId(@Param("boardId") Long boardId);

    // N+1 문제 방지 - author, comments, images 한 번에 조회
    @EntityGraph(attributePaths = {"author", "comments", "images"})
    Page<Board> findAll(Pageable pageable);

    // 특정 작성자의 게시글을 조회할 때, author, comments, images 한 번에 조회
    @EntityGraph(attributePaths = {"author", "comments", "images"})
    List<Board> getBoardByAuthor(@Param("member") Member member);

    // 특정 게시글 단건 조회 시, author, comments, images 한 번에 조회
    @EntityGraph(attributePaths = {"author", "comments", "images"})
    Optional<Board> findById(@Param("boardId") Long boardId);

}

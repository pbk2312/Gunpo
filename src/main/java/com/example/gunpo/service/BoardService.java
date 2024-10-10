package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.BoardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardService {

    // 게시물 생성 메서드
    Long createPost(BoardDto boardDto,String accessToken); // 게시물 ID를 반환

    Page<BoardDto> getPosts(Pageable pageable);
    // 게시물 조회 메서드
    Board getPost(Long postId); // 게시물 조회

    // 게시물 수정 메서드 (추가적인 예시)
    void updatePost(BoardDto boardDto); // 수정할 게시물 ID와 내용을 인수로 받음

    // 게시물 삭제 메서드 (추가적인 예시)
    void deletePost(Long postId); // 삭제할 게시물 ID
}
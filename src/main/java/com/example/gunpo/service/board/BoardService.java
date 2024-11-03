package com.example.gunpo.service.board;

import com.example.gunpo.dto.BoardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BoardService {


    Page<BoardDto> getPosts(Pageable pageable);

    // 게시물 조회 메서드
    BoardDto getPost(Long postId, String accessToken); // 게시물 조회

    // 게시물 삭제 메서드 (추가적인 예시)
    void deletePost(Long postId, String accessToken); // 삭제할 게시물 ID

}

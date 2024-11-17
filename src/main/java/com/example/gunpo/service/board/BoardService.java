package com.example.gunpo.service.board;

import com.example.gunpo.dto.BoardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BoardService {


    Page<BoardDto> getPosts(Pageable pageable);

    BoardDto getPost(Long postId, String accessToken);

    void deletePost(Long postId, String accessToken);

}

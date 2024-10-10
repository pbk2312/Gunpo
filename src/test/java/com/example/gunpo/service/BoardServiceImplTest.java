package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.exception.email.PostSaveException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @InjectMocks
    private BoardServiceImpl boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        // 테스트를 위한 Member 객체 초기화
        member = new Member();
        member.setId(1L);
        member.setEmail("test@example.com");
    }

    @Test
    void testCreatePost_Success() {
        // Given
        BoardDto boardDto = new BoardDto();
        boardDto.setTitle("Test Title");
        boardDto.setContent("Test Content");

        when(memberService.getUserDetails(any(String.class))).thenReturn(member);

        Board savedBoard = new Board();
        savedBoard.setId(1L);
        when(boardRepository.save(any(Board.class))).thenReturn(savedBoard);

        // When
        Long postId = boardService.createPost(boardDto, "validAccessToken");

        // Then
        assertNotNull(postId);
        assertEquals(1L, postId);
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testCreatePost_UnauthorizedException() {
        // Given
        BoardDto boardDto = new BoardDto();
        boardDto.setTitle("Test Title");
        boardDto.setContent("Test Content");

        when(memberService.getUserDetails(any(String.class))).thenThrow(new UnauthorizedException("게시물을 작성할 수 없는 사용자입니다."));

        // When & Then
        Exception exception = assertThrows(UnauthorizedException.class, () -> {
            boardService.createPost(boardDto, "invalidAccessToken");
        });

        assertEquals("게시물을 작성할 수 없는 사용자입니다.", exception.getMessage());
    }

    @Test
    void testCreatePost_MemberNotFoundException() {
        // Given
        BoardDto boardDto = new BoardDto();
        boardDto.setTitle("Test Title");
        boardDto.setContent("Test Content");

        when(memberService.getUserDetails(any(String.class))).thenThrow(new MemberNotFoundException("게시물을 작성할 사용자를 찾을 수 없습니다."));

        // When & Then
        Exception exception = assertThrows(MemberNotFoundException.class, () -> {
            boardService.createPost(boardDto, "validAccessToken");
        });

        assertEquals("게시물을 작성할 사용자를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void testCreatePost_PostSaveException() {
        // Given
        BoardDto boardDto = new BoardDto();
        boardDto.setTitle("Test Title");
        boardDto.setContent("Test Content");

        when(memberService.getUserDetails(any(String.class))).thenReturn(member);
        when(boardRepository.save(any(Board.class))).thenThrow(new DataAccessException("DB Error") {});

        // When & Then
        Exception exception = assertThrows(PostSaveException.class, () -> {
            boardService.createPost(boardDto, "validAccessToken");
        });

        assertEquals("게시물을 저장하는 중 오류가 발생했습니다.", exception.getMessage());
    }
}
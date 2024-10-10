package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Category;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.exception.email.PostSaveException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberService memberService;

    @Override
    public Long createPost(BoardDto boardDto, String accessToken) {
        Member member;
        try {
            member = memberService.getUserDetails(accessToken);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("게시물을 작성할 수 없는 사용자입니다.");
        } catch (MemberNotFoundException e) {
            throw new MemberNotFoundException("게시물을 작성할 사용자를 찾을 수 없습니다.");
        }

        Board board = BoardMapper.INSTANCE.toEntity(boardDto);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        board.setViewCount(0);  // 초기 조회수 설정
        board.setAuthor(member);
        board.setCategory(Category.valueOf(boardDto.getCategory()));

        try {
            Board savedBoard = boardRepository.save(board);
            return savedBoard.getId();
        } catch (DataAccessException e) {
            // 로깅 추가
            log.error("게시물 저장 중 오류 발생", e);
            throw new PostSaveException("게시물을 저장하는 중 오류가 발생했습니다."); // 사용자 정의 예외
        }
    }

    @Override
    public Page<BoardDto> getPosts(Pageable pageable) {
        Page<Board> boardPage = boardRepository.findAll(pageable);
        return boardPage.map(BoardMapper.INSTANCE::toDto); // Board를 BoardDto로 변환
    }

    @Override
    public Board getPost(Long postId) {
        return null;
    }

    @Override
    public void updatePost(BoardDto boardDto) {

    }

    @Override
    public void deletePost(Long postId) {

    }
}

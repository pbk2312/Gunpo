package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Category;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.exception.email.PostSaveException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberService memberService;
    private final RedisService redisService;

    // 게시물 작성
    @Override
    public Long createPost(BoardDto boardDto, String accessToken) {
        return saveBoard(boardDto, accessToken);
    }

    // 게시물 리스트 가져오기
    @Override
    public Page<BoardDto> getPosts(Pageable pageable) {
        return convertToDto(fetchPosts(pageable));
    }

    // 게시물 가져오기
    @Override
    public BoardDto getPost(Long postId) {
        return BoardMapper.INSTANCE.toDto(findBoard(postId));
    }

    @Override
    public void updatePost(BoardDto boardDto) {

    }

    @Override
    public void deletePost(Long postId) {

    }


    private Member validationAndRetrieveMember(String accessToken) {
        Member member;
        try {
            member = memberService.getUserDetails(accessToken);
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("게시물을 작성할 수 없는 사용자입니다.");
        } catch (MemberNotFoundException e) {
            throw new MemberNotFoundException("게시물을 작성할 사용자를 찾을 수 없습니다.");
        }
        return member;
    }

    private static Board createBoardFromDto(BoardDto boardDto, Member member) {
        Board board = BoardMapper.INSTANCE.toEntity(boardDto);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        board.setViewCount(0);  // 초기 조회수 설정
        board.setAuthor(member);
        board.setCategory(Category.valueOf(boardDto.getCategory()));
        return board;
    }

    private Long saveBoard(BoardDto boardDto, String accessToken) {
        try {
            Member member = validateMember(accessToken);
            Board board = convertDtoToBoard(boardDto, member);
            Long boardId = saveBoardToRepository(board);
            saveViewCountToRedis(boardId);
            return boardId;
        } catch (DataAccessException e) {
            log.error("게시물 저장 중 오류 발생", e);
            throw new PostSaveException("게시물을 저장하는 중 오류가 발생했습니다.");
        }
    }


    // Board 최신 정보로 가져오게 정렬 기준 확립 및 가져오기
    private Page<Board> descendingAndGetboardPage(Pageable pageable) {
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return boardRepository.findAll(sortedByCreatedAtDesc);
    }
    private Member validateMember(String accessToken) {
        return validationAndRetrieveMember(accessToken);
    }
    private Board convertDtoToBoard(BoardDto boardDto, Member member) {
        return createBoardFromDto(boardDto, member);
    }

    private Long saveBoardToRepository(Board board) {
        return boardRepository.save(board).getId();
    }
    private Page<Board> fetchPosts(Pageable pageable) {
        return descendingAndGetboardPage(pageable);
    }

    private Page<BoardDto> convertToDto(Page<Board> boardPage) {
        return boardPage.map(BoardMapper.INSTANCE::toDto);
    }


    // 게시물 Repository 에서 가져오기
    private Board findBoard(Long postId) {
        return boardRepository.findById(postId).orElseThrow(() -> new CannotFindBoardException("존재하는 Board를 찾을 수 없습니다."));
    }
    private void saveViewCountToRedis(Long boardId) {
        redisService.saveViewCountToRedis(boardId);
    }
}

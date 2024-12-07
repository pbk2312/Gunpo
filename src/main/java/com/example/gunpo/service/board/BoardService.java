package com.example.gunpo.service.board;

import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.board.BoardDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.exception.board.InvalidPageableException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.board.RedisViewCountService;
import com.example.gunpo.validator.board.BoardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardService {

    private static final String CREATED_AT_FIELD = "createdAt";
    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final RedisViewCountService redisViewCountService;
    private final BoardValidator boardValidator;
    private final AuthenticationService authenticationService;

    @Transactional(readOnly = true)
    public Page<BoardDto> getPosts(Pageable pageable) {
        validatePageable(pageable);

        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD)
        );

        log.info("게시물 조회 요청 - 페이지 번호: {}, 페이지 크기: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<BoardDto> boardDtos = boardRepository.findAll(sortedByCreatedAtDesc).map(boardMapper::toDto);

        setViewCounts(boardDtos.getContent());

        log.info("게시물 조회 완료 - 총 게시물 수: {}", boardDtos.getTotalElements());

        return boardDtos;
    }

    @Transactional(readOnly = true)
    public BoardDto getPost(Long postId, String accessToken) {
        Board board = getBoard(postId);
        Member member = getMember(accessToken);

        redisViewCountService.incrementViewCountIfNotExists(postId, member.getId());

        BoardDto boardDto = boardMapper.toDto(board);
        setViewCount(boardDto, board.getId());

        return boardDto;
    }

    private Member getMember(String accessToken) {
        return authenticationService.getUserDetails(accessToken);
    }

    @Transactional
    public void deletePost(Long postId, String accessToken) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);

        Board board = getBoard(postId);
        boardValidator.verifyAuthor(board, accessToken);

        boardRepository.delete(board);
        log.info("게시물이 성공적으로 삭제되었습니다. ID: {}", postId);
    }

    @Transactional(readOnly = true)
    public List<BoardDto> getPostListByMember(String accessToken) {
        // 인증된 사용자의 정보를 가져옴
        Member member = getMember(accessToken);

        // 해당 사용자가 작성한 게시물 리스트를 조회
        List<Board> boardByAuthor = boardRepository.getBoardByAuthor(member);

        return boardByAuthor.stream()
                .map(boardMapper::toDto) // 메서드 참조로 변경
                .toList();
    }

    public List<BoardDto> getPostListByAuthorName(String nickName) {
        Member member = authenticationService.getMemberByNickname(nickName);
        List<Board> boardByAuthor = boardRepository.getBoardByAuthor(member);

        return boardByAuthor.stream()
                .map(boardMapper::toDto)
                .toList();
    }


    private Board getBoard(Long postId) {
        boardValidator.validatePostId(postId);
        return boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.POST_NOT_FOUND.getMessage()));
    }

    private void setViewCount(BoardDto boardDto, Long boardId) {
        int viewCount = redisViewCountService.getViewCount(boardId);
        boardDto.setViewCount(viewCount);
    }


    private void setViewCounts(List<BoardDto> boardDtos) {
        List<Long> boardIds = boardDtos.stream()
                .map(BoardDto::getId)
                .toList();
        Map<Long, Integer> viewCounts = redisViewCountService.getViewCounts(boardIds);

        boardDtos.forEach(dto -> dto.setViewCount(viewCounts.getOrDefault(dto.getId(), 0)));
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() <= 0) {
            throw new InvalidPageableException(BoardErrorMessage.INVALID_PAGEABLE.getMessage());
        }
    }

}

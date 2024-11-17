package com.example.gunpo.service.board;

import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.redis.RedisViewCountService;
import com.example.gunpo.validator.board.BoardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {

    private static final String CREATED_AT_FIELD = "createdAt";

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final RedisViewCountService redisViewCountService;
    private final BoardValidator boardValidator;

    @Override
    @Transactional
    public Page<BoardDto> getPosts(Pageable pageable) {
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, CREATED_AT_FIELD)
        );

        return boardRepository.findAll(sortedByCreatedAtDesc).map(board -> {
            BoardDto boardDto = boardMapper.toDto(board);
            setViewCount(boardDto, board.getId());
            return boardDto;
        });
    }

    @Override
    @Transactional
    public BoardDto getPost(Long postId, String accessToken) {
        boardValidator.validatePostId(postId);
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("게시물을 찾을 수 없습니다. ID: " + postId));

        redisViewCountService.incrementViewCountIfNotExists(postId, accessToken);

        BoardDto boardDto = boardMapper.toDto(board);
        setViewCount(boardDto, board.getId());

        return boardDto;
    }

    @Override
    public void deletePost(Long postId, String accessToken) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("게시물을 찾을 수 없습니다. ID: " + postId));
        boardValidator.verifyAuthor(board, accessToken);

        boardRepository.delete(board);
        log.info("게시물이 성공적으로 삭제되었습니다. ID: {}", postId);

    }

    // 조회수 설정 메서드
    private void setViewCount(BoardDto boardDto, Long boardId) {
        int viewCount = redisViewCountService.getViewCountFromRedis(boardId);
        boardDto.setViewCount(viewCount);
    }

}

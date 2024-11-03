package com.example.gunpo.service.board;

import com.example.gunpo.domain.Board;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.mapper.BoardMapper;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.validator.board.BoardValidator;
import com.example.gunpo.validator.board.PostIdValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;
    private final BoardUpdateService boardUpdateService;
    private final PostIdValidator postIdValidator;
    private final BoardValidator boardValidator;


    @Override
    @Transactional
    public Page<BoardDto> getPosts(Pageable pageable) {
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        log.info("게시물 정렬 기준 설정 및 조회 시작");
        return boardRepository.findAll(sortedByCreatedAtDesc).map(boardMapper::toDto);
    }

    @Override
    @Transactional
    public BoardDto getPost(Long postId, String accessToken) {
        postIdValidator.validate(postId);
        Board board = boardRepository.findById(postId).orElseThrow(() -> new CannotFindBoardException("찾을 수 없다."));
        return boardMapper.toDto(board);
    }

    @Override
    public void deletePost(Long postId, String accessToken) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("게시물을 찾을 수 없습니다."));
        boardValidator.verifyAuthor(board, accessToken);

        boardRepository.delete(board);
        log.info("게시물이 성공적으로 삭제되었습니다. ID: {}", postId);
    }


}

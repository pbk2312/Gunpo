package com.example.gunpo.service;
import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.handler.AuthorizationHandler;
import com.example.gunpo.repository.BoardRepository;

import com.example.gunpo.validator.AccessTokenValidator;
import com.example.gunpo.validator.BoardValidator;
import com.example.gunpo.validator.PostIdValidator;
import com.example.gunpo.validator.Validator;
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
    private final MemberService memberService;
    private final RedisService redisService;
    private final AuthorizationHandler authorizationHandler;
    private final ImageProcessor imageProcessor;

    private final Validator<BoardDto> boardValidator = new BoardValidator();
    private final Validator<String> accessTokenValidator = new AccessTokenValidator();
    private final Validator<Long> postIdValidator = new PostIdValidator();

    @Override
    public Long createPost(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        boardValidator.validate(boardDto);
        accessTokenValidator.validate(accessToken);

        log.info("게시물 작성 요청 - 제목: {}, 사용자 토큰: {}", boardDto.getTitle(), accessToken);

        return saveBoardWithDetails(boardDto, accessToken, images);
    }

    private Long saveBoardWithDetails(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        Member member = memberService.getUserDetails(accessToken);
        Board board = BoardFactory.createBoard(boardDto, member);

        Long boardId = boardRepository.save(board).getId();
        redisService.saveViewCountToRedis(boardId);
        imageProcessor.processNewImages(images, board);

        log.info("게시물 저장 완료 - 게시물 ID: {}", boardId);
        return boardId;
    }

    @Override
    @Transactional
    public Page<BoardDto> getPosts(Pageable pageable) {
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        log.info("게시물 정렬 기준 설정 및 조회 시작");
        return boardRepository.findAll(sortedByCreatedAtDesc).map(this::convertToDto);
    }

    @Override
    @Transactional
    public BoardDto getPost(Long postId, String accessToken) {
        postIdValidator.validate(postId);
        accessTokenValidator.validate(accessToken);

        log.info("게시물 조회 요청 - 게시물 ID: {}, 사용자 토큰: {}", postId, accessToken);

        Member member = memberService.getUserDetails(accessToken);
        String userId = member.getId().toString();

        redisService.incrementViewCountIfNotExists(postId, userId);
        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("존재하는 게시물을 찾을 수 없습니다."));

        return convertToDto(board);
    }

    private BoardDto convertToDto(Board board) {
        int viewCount = redisService.getViewCountFromRedis(board.getId());
        List<String> imagePaths = imageProcessor.extractImagePaths(board);

        return BoardDto.builder()
                .id(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .authorId(board.getAuthor().getId())
                .nickname(board.getAuthor().getNickname())
                .createdAt(board.getCreatedAt())
                .updatedAt(board.getUpdatedAt())
                .viewCount(viewCount)
                .imagePaths(imagePaths)
                .category(board.getCategory())
                .build();
    }

    @Override
    @Transactional
    public void updatePost(BoardDto boardDto, List<MultipartFile> newImages, List<String> deleteImages, String accessToken) {
        log.info("게시물 업데이트 요청 - 제목: {}, 내용: {}, 카테고리: {}", boardDto.getTitle(), boardDto.getContent(), boardDto.getCategory());

        Board existingBoard = boardRepository.findById(boardDto.getId())
                .orElseThrow(() -> new CannotFindBoardException("존재하는 게시물을 찾을 수 없습니다."));
        authorizationHandler.verifyAuthor(existingBoard, accessToken);

        imageProcessor.processDeletedImages(deleteImages, existingBoard);
        List<BoardImage> updatedImages = imageProcessor.processNewImages(newImages, existingBoard);

        Board updatedBoard = BoardFactory.createUpdatedBoard(existingBoard, boardDto, updatedImages);
        boardRepository.save(updatedBoard);

        log.info("게시물 업데이트 완료 - 게시물 ID: {}", updatedBoard.getId());
    }

    @Override
    public void deletePost(Long postId, String accessToken) {
        log.info("게시물 삭제 요청 - 게시물 ID: {}", postId);

        Board board = boardRepository.findById(postId)
                .orElseThrow(() -> new CannotFindBoardException("게시물을 찾을 수 없습니다."));
        authorizationHandler.verifyAuthor(board, accessToken);

        boardRepository.delete(board);
        log.info("게시물이 성공적으로 삭제되었습니다. ID: {}", postId);
    }
}

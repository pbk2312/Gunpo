package com.example.gunpo.service.board;

import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.RedisService;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.validator.AccessTokenValidator;
import com.example.gunpo.validator.BoardValidator;
import com.example.gunpo.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardCreationService {
    private final BoardRepository boardRepository;
    private final RedisService redisService;
    private final AuthenticationService authenticationService;
    private final ImageProcessor imageProcessor;

    private final Validator<BoardDto> boardValidator = new BoardValidator();
    private final Validator<String> accessTokenValidator = new AccessTokenValidator();
    public Long create(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        boardValidator.validate(boardDto);
        accessTokenValidator.validate(accessToken);

        log.info("게시물 작성 요청 - 제목: {}, 사용자 토큰: {}", boardDto.getTitle(), accessToken);

        return saveBoardWithDetails(boardDto, accessToken, images);
    }

    private Long saveBoardWithDetails(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        Member member = authenticationService.getUserDetails(accessToken);
        Board board = BoardFactory.createBoard(boardDto, member);

        Long boardId = boardRepository.save(board).getId();
        redisService.saveViewCountToRedis(boardId);
        imageProcessor.processNewImages(images, board);

        log.info("게시물 저장 완료 - 게시물 ID: {}", boardId);
        return boardId;
    }
}

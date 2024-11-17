package com.example.gunpo.service.board;

import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.member.AuthenticationService;
import com.example.gunpo.service.redis.RedisViewCountService;
import com.example.gunpo.validator.board.BoardValidator;
import com.example.gunpo.validator.member.AuthenticationValidator;
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
    private final RedisViewCountService redisViewCountService;
    private final AuthenticationService authenticationService;
    private final ImageProcessor imageProcessor;
    private final AuthenticationValidator authenticationValidator;
    private final BoardValidator boardValidator;

    public Long create(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        boardValidator.validate(boardDto);
        authenticationValidator.validateAccessToken(accessToken);

        log.info("게시물 작성 요청 - 제목: {}, 사용자 토큰: {}", boardDto.getTitle(), accessToken);

        return saveBoardWithDetails(boardDto, accessToken, images);

    }

    private Long saveBoardWithDetails(BoardDto boardDto, String accessToken, List<MultipartFile> images) {
        Member member = authenticationService.getUserDetails(accessToken);
        Board board = BoardFactory.createBoard(boardDto, member);

        Long boardId = boardRepository.save(board).getId();
        redisViewCountService.saveViewCountToRedis(boardId);
        imageProcessor.processNewImages(images, board);

        log.info("게시물 저장 완료 - 게시물 ID: {}", boardId);
        return boardId;

    }

}

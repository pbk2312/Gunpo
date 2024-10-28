package com.example.gunpo.service.board;

import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.handler.AuthorizationHandler;
import com.example.gunpo.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardUpdateService {
    private final BoardRepository boardRepository;
    private final AuthorizationHandler authorizationHandler;
    private final ImageProcessor imageProcessor;

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


}

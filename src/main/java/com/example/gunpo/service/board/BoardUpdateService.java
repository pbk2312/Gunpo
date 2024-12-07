package com.example.gunpo.service.board;

import com.example.gunpo.service.image.ImageProcessor;
import com.example.gunpo.constants.errorMessage.BoardErrorMessage;
import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.board.BoardImage;
import com.example.gunpo.dto.board.BoardDto;
import com.example.gunpo.exception.board.CannotFindBoardException;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.validator.board.BoardValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class BoardUpdateService {
    private final BoardRepository boardRepository;
    private final ImageProcessor imageProcessor;
    private final BoardValidator boardValidator;

    @Transactional
    public void updatePost(BoardDto boardDto, List<MultipartFile> newImages, List<String> deleteImages,
                           String accessToken) {

        // 기존 게시글 조회
        Board existingBoard = boardRepository.findById(boardDto.getId())
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.POST_NOT_FOUND.getMessage()));

        // 작성자 검증
        boardValidator.verifyAuthor(existingBoard, accessToken);

        // 삭제할 이미지 처리
        imageProcessor.processDeletedImages(deleteImages, existingBoard);

        // 추가할 새 이미지 처리
        List<BoardImage> updatedImages = imageProcessor.processNewImages(newImages, existingBoard);

        Board updatedBoard = existingBoard.updateBoard(
                boardDto.getTitle(),
                boardDto.getContent(),
                boardDto.getCategory(),
                updatedImages
        );

        boardRepository.save(updatedBoard);
    }

}

package com.example.gunpo.service.board;

import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.constants.BoardErrorMessage;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.dto.BoardDto;
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

        Board existingBoard = boardRepository.findById(boardDto.getId())
                .orElseThrow(() -> new CannotFindBoardException(BoardErrorMessage.POST_NOT_FOUND.getMessage()));

        boardValidator.verifyAuthor(existingBoard, accessToken);

        imageProcessor.processDeletedImages(deleteImages, existingBoard);
        List<BoardImage> updatedImages = imageProcessor.processNewImages(newImages, existingBoard);

        Board updatedBoard = BoardFactory.createUpdatedBoard(existingBoard, boardDto, updatedImages);
        boardRepository.save(updatedBoard);

    }

}

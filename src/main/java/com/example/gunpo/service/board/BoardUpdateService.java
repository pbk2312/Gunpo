package com.example.gunpo.service.board;

import com.example.gunpo.Factory.BoardFactory;
import com.example.gunpo.Factory.ImageProcessor;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.BoardDto;
import com.example.gunpo.exception.CannotFindBoardException;
import com.example.gunpo.exception.UnauthorizedException;
import com.example.gunpo.repository.BoardRepository;
import com.example.gunpo.service.member.AuthenticationService;
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
    private final ImageProcessor imageProcessor;
    private final AuthenticationService authenticationService;

    public void updatePost(BoardDto boardDto, List<MultipartFile> newImages, List<String> deleteImages,
                           String accessToken) {

        Board existingBoard = boardRepository.findById(boardDto.getId())
                .orElseThrow(() -> new CannotFindBoardException("존재하는 게시물을 찾을 수 없습니다."));
        verifyAuthor(existingBoard, accessToken);

        imageProcessor.processDeletedImages(deleteImages, existingBoard);
        List<BoardImage> updatedImages = imageProcessor.processNewImages(newImages, existingBoard);

        Board updatedBoard = BoardFactory.createUpdatedBoard(existingBoard, boardDto, updatedImages);
        boardRepository.save(updatedBoard);

    }

    public void verifyAuthor(Board board, String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        if (!board.getAuthor().getEmail().equals(member.getEmail())) {
            throw new UnauthorizedException("게시물 수정 권한이 없습니다.");
        }
    }


}

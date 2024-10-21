package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.repository.BoardImageRepository;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    private final BoardImageRepository boardImageRepository;


    // 이미지 저장 및 게시물에 추가, 저장된 이미지를 반환
    public List<BoardImage> saveImages(List<MultipartFile> images, Board board) {
        List<BoardImage> savedImages = new ArrayList<>(); // 저장된 이미지 리스트

        if (images != null && !images.isEmpty()) {
            log.info("업로드된 이미지 수: {}", images.size());

            for (MultipartFile image : images) {
                if (image.isEmpty()) {
                    log.warn("비어있는 이미지 파일은 저장하지 않습니다.");
                    continue; // 비어있는 이미지는 무시
                }

                // 이미지 저장
                String imagePath = saveImage(image);
                log.info("이미지 저장 성공 - 경로: {}", imagePath);

                // BoardImage 객체 생성 및 게시물에 추가
                BoardImage boardImage = createBoardImage(imagePath, board);
                board.addImage(boardImage); // Board에 이미지 추가

                // BoardImage를 DB에 저장
                boardImageRepository.save(boardImage);

                // 저장된 이미지를 리스트에 추가
                savedImages.add(boardImage);
            }
        } else {
            log.info("업로드된 이미지가 없습니다.");
        }

        return savedImages; // 저장된 이미지 리스트 반환
    }

    // 실제 파일을 저장하는 로직
    public String saveImage(MultipartFile file) {
        try {
            // 파일명 생성 (UUID 사용)
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File uploadFile = new File(uploadDir, fileName);

            // 디렉토리가 없으면 생성
            if (!uploadFile.getParentFile().exists()) {
                uploadFile.getParentFile().mkdirs();
            }

            // 파일 저장
            file.transferTo(uploadFile);

            // 저장된 파일 경로 반환
            return "/Gunpo/" + fileName;
        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.");
        }
    }



    // BoardImage 객체 생성
    private BoardImage createBoardImage(String imagePath, Board board) {
        return BoardImage.builder()
                .imagePath(imagePath)
                .board(board)
                .build();
    }
}
package com.example.gunpo.service;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.repository.BoardImageRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    private final BoardImageRepository boardImageRepository;

    public List<BoardImage> saveImages(List<MultipartFile> images, Board board) {
        List<BoardImage> savedImages = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            log.info("업로드된 이미지가 없습니다.");
            return savedImages;
        }

        log.info("업로드된 이미지 수: {}", images.size());

        for (MultipartFile image : images) {
            processSingleImage(image, board).ifPresent(savedImages::add);
        }

        return savedImages;
    }

    private Optional<BoardImage> processSingleImage(MultipartFile image, Board board) {
        if (image.isEmpty()) {
            log.warn("비어있는 이미지 파일은 저장하지 않습니다.");
            return Optional.empty();
        }

        String imagePath = storeImage(image);
        BoardImage boardImage = createBoardImage(imagePath, board);
        board.addImage(boardImage);

        log.info("이미지 저장 성공 - 경로: {}", imagePath);

        boardImageRepository.save(boardImage);
        return Optional.of(boardImage);
    }

    // 실제 파일을 저장하는 로직
    private String storeImage(MultipartFile file) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        File uploadFile = new File(uploadDir, fileName);

        ensureDirectoryExists(uploadFile.getParentFile());

        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.");
        }

        return "/Gunpo/" + fileName;
    }

    private void ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    public void deleteImage(String imagePath) {
        String filePath = getFilePathFromImagePath(imagePath);
        deleteFile(filePath);
        deleteImageData(imagePath);
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);

        if (file.exists()) {
            if (file.delete()) {
                log.info("이미지 파일 삭제 성공 - 경로: {}", filePath);
            } else {
                log.warn("이미지 파일 삭제 실패 - 경로: {}", filePath);
            }
        } else {
            log.warn("이미지 파일을 찾을 수 없음 - 경로: {}", filePath);
        }
    }

    private void deleteImageData(String imagePath) {
        BoardImage boardImage = boardImageRepository.findByImagePath(imagePath);
        if (boardImage != null) {
            boardImageRepository.delete(boardImage);
            log.info("이미지 정보 삭제 완료 - 경로: {}", imagePath);
        } else {
            log.warn("이미지 데이터베이스에서 찾을 수 없음 - 경로: {}", imagePath);
        }
    }

    private String getFilePathFromImagePath(String imagePath) {
        return uploadDir + imagePath.substring(imagePath.lastIndexOf("/"));
    }

    private BoardImage createBoardImage(String imagePath, Board board) {
        return BoardImage.builder()
                .imagePath(imagePath)
                .board(board)
                .build();
    }
}

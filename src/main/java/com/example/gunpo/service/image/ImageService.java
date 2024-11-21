package com.example.gunpo.service.image;

import com.example.gunpo.constants.errorMessage.ImageErrorMessage;
import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.exception.board.ImageStorageException;
import com.example.gunpo.repository.BoardImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    private final BoardImageRepository boardImageRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB (파일 크기 제한)
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};

    @Transactional
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

        // 이미지 유효성 검증
        if (!isValidFileFormat(image)) {
            log.warn("지원되지 않는 파일 형식입니다. 파일명: {}", image.getOriginalFilename());
            throw new ImageStorageException(ImageErrorMessage.INVALID_FILE_FORMAT.getMessage());
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            log.warn("파일 크기가 너무 큽니다. 파일명: {}", image.getOriginalFilename());
            throw new ImageStorageException(ImageErrorMessage.FILE_TOO_LARGE.getMessage());
        }

        String imagePath = storeImage(image);
        BoardImage boardImage = createBoardImage(imagePath, board);
        board.addImage(boardImage);

        log.info("이미지 저장 성공 - 경로: {}", imagePath);

        boardImageRepository.save(boardImage);
        return Optional.of(boardImage);
    }

    private String storeImage(MultipartFile file) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        File uploadFile = new File(uploadDir, fileName);

        ensureDirectoryExists(uploadFile.getParentFile());

        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            log.error("이미지 저장 중 오류 발생: {}", e.getMessage());
            throw new ImageStorageException(ImageErrorMessage.IMAGE_STORAGE_ERROR.getMessage(), e);
        }

        return "/Gunpo/" + fileName;
    }

    private void ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                log.error("디렉토리 생성 실패 - 경로: {}", directory.getAbsolutePath());
                throw new ImageStorageException("디렉토리 생성 중 오류가 발생했습니다.");
            }
        }
    }

    private String generateUniqueFileName(String originalFilename) {
        return UUID.randomUUID() + "_" + originalFilename;
    }

    private boolean isValidFileFormat(MultipartFile file) {
        String fileName = file.getOriginalFilename();

        // 파일 이름이 null인 경우 처리
        if (fileName == null) {
            log.warn("파일 이름이 null입니다. 파일을 저장할 수 없습니다.");
            return false;
        }

        String fileExtension = getFileExtension(fileName);
        for (String ext : ALLOWED_EXTENSIONS) {
            if (fileExtension.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        return (index > 0) ? fileName.substring(index) : "";
    }

    @Transactional
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

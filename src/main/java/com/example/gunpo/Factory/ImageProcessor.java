package com.example.gunpo.Factory;

import com.example.gunpo.domain.Board;
import com.example.gunpo.domain.BoardImage;
import com.example.gunpo.service.ImageService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageProcessor {
    private final ImageService imageService;

    public ImageProcessor(ImageService imageService) {
        this.imageService = imageService;
    }

    public void processDeletedImages(List<String> deleteImages, Board board) {
        if (deleteImages == null || deleteImages.isEmpty()) return;

        for (String imagePath : deleteImages) {
            imageService.deleteImage(imagePath);
            board.getImages().removeIf(image -> image.getImagePath().equals(imagePath));
        }
    }

    public List<BoardImage> processNewImages(List<MultipartFile> newImages, Board board) {
        List<BoardImage> existingImages = board.getImages() != null ? new ArrayList<>(board.getImages()) : new ArrayList<>();

        if (newImages != null && !newImages.isEmpty()) {
            List<BoardImage> newSavedImages = imageService.saveImages(newImages, board);
            existingImages.addAll(newSavedImages);
        }
        return existingImages;
    }

    // 추가된 메서드
    public List<String> extractImagePaths(Board board) {
        if (board.getImages() == null || board.getImages().isEmpty()) {
            return Collections.emptyList();
        }
        return board.getImages().stream()
                .map(BoardImage::getImagePath)
                .collect(Collectors.toList());
    }
}

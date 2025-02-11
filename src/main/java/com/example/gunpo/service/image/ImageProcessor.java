package com.example.gunpo.service.image;

import com.example.gunpo.domain.board.Board;
import com.example.gunpo.domain.board.BoardImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ImageProcessor {
    private final ImageService imageService;

    public void processDeletedImages(Set<String> deleteImages, Board board) {
        if (deleteImages == null || deleteImages.isEmpty()) return;

        for (String imagePath : deleteImages) {
            imageService.deleteImage(imagePath);
            board.getImages().removeIf(image -> image.getImagePath().equals(imagePath));
        }
    }

    public Set<BoardImage> processNewImages(Set<MultipartFile> newImages, Board board) {
        Set<BoardImage> existingImages = board.getImages() != null ? new HashSet<>(board.getImages()) : new HashSet<>();

        if (newImages != null && !newImages.isEmpty()) {
            Set<BoardImage> newSavedImages = imageService.saveImages(newImages, board);
            existingImages.addAll(newSavedImages);
        }
        return existingImages;
    }

    public Set<String> extractImagePaths(Board board) {
        if (board.getImages() == null || board.getImages().isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> imagePaths = new HashSet<>();
        for (BoardImage image : board.getImages()) {
            imagePaths.add(image.getImagePath());
        }
        return imagePaths;
    }
}

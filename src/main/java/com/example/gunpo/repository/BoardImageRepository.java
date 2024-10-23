package com.example.gunpo.repository;

import com.example.gunpo.domain.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {

    // 이미지 경로로 BoardImage 조회
    BoardImage findByImagePath(String imagePath);

    // 이미지 경로로 이미지 삭제
    void deleteByImagePath(String imagePath);

}

package com.example.gunpo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;  // 카테고리 추가

    @Builder
    public Inquiry(String message, InquiryCategory category, LocalDateTime createdAt) {
        this.message = message;
        this.category = category;
        this.createdAt = createdAt;
    }

}

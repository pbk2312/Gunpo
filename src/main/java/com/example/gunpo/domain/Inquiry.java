package com.example.gunpo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;  // 카테고리 추가

    @ManyToOne(fetch = FetchType.LAZY)  // 회원과의 N:1
    @JoinColumn(name = "member_id")
    private Member member;

    public static Inquiry create(String message, InquiryCategory category, Member member) {
        return Inquiry.builder()
                .message(message)
                .category(category)
                .createdAt(LocalDateTime.now())
                .member(member)
                .build();
    }

}
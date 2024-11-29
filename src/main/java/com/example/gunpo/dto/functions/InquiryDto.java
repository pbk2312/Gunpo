package com.example.gunpo.dto.functions;


import com.example.gunpo.domain.InquiryCategory;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InquiryDto {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "문의 내용은 필수입니다.")
    private String message;

    @Enumerated(EnumType.STRING)
    @NotBlank(message = "카테고리는 필수입니다.")
    private InquiryCategory inquiryCategory;


    private LocalDateTime createdAt;

}

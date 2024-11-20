package com.example.gunpo.dto;

import com.example.gunpo.domain.Member;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDto {

    private Long id;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    private LocalDate dateOfBirth;

    public MemberUpdateDto(Member member) {
        this.id = member.getId();
        this.nickname = member.getNickname();
        this.dateOfBirth = member.getDateOfBirth();
    }

}

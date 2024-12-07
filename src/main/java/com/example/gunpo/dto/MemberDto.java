package com.example.gunpo.dto;


import com.example.gunpo.domain.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder
public class MemberDto {


    private Long id;

    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp="(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}",
            message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    private String password;

    @NotBlank
    private String checkPassword;

    private MemberRole memberRole;

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    private String nickname;

    @PastOrPresent(message = "생년월일은 현재 날짜 이전이어야 합니다.")
    private LocalDate dateOfBirth;

    private String certificationNumber;

}

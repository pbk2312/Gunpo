package com.example.gunpo.domain;

import com.example.gunpo.dto.MemberDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;

    private String nickname;

    private LocalDate dateOfBirth;

    private boolean neighborhoodVerification;

    @OneToMany(mappedBy = "author")
    @Builder.Default
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "member") // 1:N 관계 추가
    @Builder.Default
    private List<Inquiry> inquiries = new ArrayList<>();

    public Member verifyNeighborhood() {
        return this.toBuilder()
                .neighborhoodVerification(true)
                .build();
    }

    public Member updateProfile(String nickname, LocalDate dateOfBirth) {
        return this.toBuilder()
                .nickname(nickname)
                .dateOfBirth(dateOfBirth)
                .build();
    }

    public static Member create(MemberDto memberDto, String encodedPassword) {
        return Member.builder()
                .email(memberDto.getEmail())
                .password(encodedPassword)
                .nickname(memberDto.getNickname())
                .dateOfBirth(memberDto.getDateOfBirth())
                .memberRole(MemberRole.MEMBER)
                .neighborhoodVerification(false)
                .build();
    }

}

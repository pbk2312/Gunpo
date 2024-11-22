package com.example.gunpo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
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

    private boolean NeighborhoodVerification;

    @OneToMany(mappedBy = "author")
    private List<Board> boards = new ArrayList<>();

}

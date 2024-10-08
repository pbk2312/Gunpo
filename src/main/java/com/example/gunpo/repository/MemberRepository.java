package com.example.gunpo.repository;

import com.example.gunpo.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findByEmail(String email);
    // Optional을 사용함으로써,이메일에 해당하는 사용자가 없을 때 null 대신 빈 Optional을 반환,(Null Pointer Exception 방지)

}

package com.example.gunpo.repository;

import com.example.gunpo.domain.Inquiry;
import com.example.gunpo.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry,Long> {

    List<Inquiry> findByMember(Member member);

}

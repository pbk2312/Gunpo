package com.example.gunpo.repository;

import com.example.gunpo.domain.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry,Long> {
}

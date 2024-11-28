package com.example.gunpo.repository;

import com.example.gunpo.domain.SmokingArea;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmokingAreaRepository extends JpaRepository<SmokingArea,Long> {

    boolean existsByBoothName(String boothName);

}

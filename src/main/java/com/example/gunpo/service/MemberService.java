package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;

public interface MemberService {

    String save(MemberDto memberDto);

    String delete(Member member);



}

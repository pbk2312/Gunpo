package com.example.gunpo.service;

import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.exception.MemberNotFoundException;

public interface MemberService {

    /**
     * 회원 정보를 저장합니다.
     * @param memberDto 저장할 회원 정보
     * @return 저장된 회원의 ID
     */
    Long save(MemberDto memberDto);

    /**
     * 회원 정보를 삭제합니다.
     * @param memberDto 삭제할 회원 정보
     * @throws MemberNotFoundException 해당 회원이 존재하지 않는 경우
     */
    void delete(MemberDto memberDto) throws MemberNotFoundException;

    /**
     * 회원 정보를 업데이트합니다.
     * @param memberDto 업데이트할 회원 정보
     * @return 업데이트된 회원의 ID
     */
    Long update(MemberDto memberDto);

    /**
     * 회원 로그인 기능을 수행합니다.
     * @param memberDto 로그인할 회원 정보
     * @return 로그인한 회원 정보
     */
    Member login(MemberDto memberDto);
}
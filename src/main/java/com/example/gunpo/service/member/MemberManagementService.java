package com.example.gunpo.service.member;

import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.MemberUpdateDto;
import com.example.gunpo.exception.member.MemberNotFoundException;

public interface MemberManagementService {

    /**
     * 회원 정보를 저장합니다.
     * @param memberDto 저장할 회원 정보
     * @return 저장된 회원의 ID
     */
    Long save(MemberDto memberDto);

    /**
     * 회원 정보를 삭제합니다.
     * @param memberId 삭제할 회원의 ID
     * @throws MemberNotFoundException 해당 회원이 존재하지 않는 경우
     */
    void delete(Long memberId) throws MemberNotFoundException;

    /**
     * 회원 정보를 업데이트합니다.
     * @param updateDto 업데이트할 회원 정보
     */
    void update(MemberUpdateDto updateDto);

}

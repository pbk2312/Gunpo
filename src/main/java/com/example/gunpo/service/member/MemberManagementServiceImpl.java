package com.example.gunpo.service.member;

import com.example.gunpo.domain.Member;
import com.example.gunpo.domain.MemberRole;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.validator.MemberRegistrationValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberManagementServiceImpl implements MemberManagementService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRegistrationValidator memberRegistrationValidator;

    @Override
    public Long save(@Valid MemberDto memberDto) {
        log.info("회원 저장 요청: {}", memberDto);
        memberRegistrationValidator.validateNewMember(memberDto);
        Member member = createMember(memberDto);
        Member savedMember = memberRepository.save(member);
        log.info("회원 저장 성공, ID: {}", savedMember.getId());
        return savedMember.getId();
    }

    @Override
    public void delete(Long memberId) {
        Member member = memberRegistrationValidator.validateExistingMember(memberId);
        memberRepository.delete(member);
        log.info("회원 삭제 성공, ID: {}", member.getId());
    }

    @Override
    public void update(MemberDto memberDto) {
        Member updatedMember = findAndUpdateMemberFields(memberDto);
        log.info("회원 정보 업데이트 성공: ID = {}", updatedMember.getId());
    }

    private Member findAndUpdateMemberFields(MemberDto memberDto) {
        Member existingMember = memberRegistrationValidator.validateExistingMember(memberDto.getId());
        existingMember.setEmail(memberDto.getEmail());
        existingMember.setDateOfBirth(memberDto.getDateOfBirth());
        existingMember.setMemberRole(memberDto.getMemberRole());
        existingMember.setNickname(memberDto.getNickname());
        return memberRepository.save(existingMember);
    }

    private Member createMember(MemberDto memberDto) {
        Member member = MemberMapper.INSTANCE.toEntity(memberDto);
        member.setPassword(passwordEncoder.encode(memberDto.getPassword()));
        member.setMemberRole(MemberRole.MEMBER);
        return member;
    }

}

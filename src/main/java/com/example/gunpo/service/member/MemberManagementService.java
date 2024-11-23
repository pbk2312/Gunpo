package com.example.gunpo.service.member;

import com.example.gunpo.constants.errorMessage.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.domain.MemberRole;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.MemberUpdateDto;
import com.example.gunpo.exception.member.MemberNotFoundException;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.repository.MemberRepository;
import com.example.gunpo.validator.member.MemberRegistrationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberManagementService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRegistrationValidator memberRegistrationValidator;
    private final AuthenticationService authenticationService;

    public void save(MemberDto memberDto) {
        memberRegistrationValidator.validateNewMember(memberDto);
        Member member = createMember(memberDto);
        memberRepository.save(member);
    }

    public void delete(String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        memberRepository.delete(member);
    }

    public void update(MemberUpdateDto updateDto) {
        Member existingMember = getExistingMember(updateDto);
        existingMember.updateMemberFields(updateDto.getNickname(), updateDto.getDateOfBirth());
    }

    public void NeighborhoodVerification(String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        member.setNeighborhoodVerification(true);
        memberRepository.save(member);
    }



    private Member getExistingMember(MemberUpdateDto updateDto) {
        return memberRepository.findById(updateDto.getId())
                .orElseThrow(() -> new MemberNotFoundException(
                        MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage()));
    }

    private Member createMember(MemberDto memberDto) {
        Member member = MemberMapper.INSTANCE.toEntity(memberDto);
        encodePassword(member);
        member.setMemberRole(MemberRole.MEMBER);
        member.setNeighborhoodVerification(false);
        return member;
    }

    private void encodePassword(Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
    }

}

package com.example.gunpo.service.member;

import com.example.gunpo.constants.errorMessage.MemberErrorMessage;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.member.MemberUpdateDto;
import com.example.gunpo.exception.member.MemberNotFoundException;
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

        // 엔티티의 팩토리 메서드를 사용하여 Member 생성
        Member member = Member.create(memberDto, encodePassword(memberDto.getPassword()));
        memberRepository.save(member);
    }

    public void delete(String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        memberRepository.delete(member);
    }

    public void update(MemberUpdateDto updateDto) {
        // 엔티티의 업데이트 메서드 사용
        Member existingMember = getExistingMember(updateDto);
        Member updatedMember = existingMember.updateProfile(
                updateDto.getNickname(),
                updateDto.getDateOfBirth()
        );
        memberRepository.save(updatedMember);

        log.info("회원 정보 업데이트 성공: ID = {}", updatedMember.getId());
    }

    public void NeighborhoodVerification(String accessToken) {
        // 상태 변경을 엔티티에 위임
        Member member = authenticationService.getUserDetails(accessToken);
        Member updatedMember = member.verifyNeighborhood();
        memberRepository.save(updatedMember);
    }

    private Member getExistingMember(MemberUpdateDto updateDto) {
        return memberRepository.findById(updateDto.getId())
                .orElseThrow(() -> new MemberNotFoundException(
                        MemberErrorMessage.MEMBER_NOT_FOUND_ID.getMessage()));
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

}

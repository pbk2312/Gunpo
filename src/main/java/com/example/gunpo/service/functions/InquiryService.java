package com.example.gunpo.service.functions;


import com.example.gunpo.domain.Inquiry;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.InquiryDto;
import com.example.gunpo.repository.InquiryRepository;
import com.example.gunpo.service.member.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final AuthenticationService authenticationService;

    public void saveInquiry(InquiryDto inquiryDto, String accessToken) {
        Member member = authenticationService.getUserDetails(accessToken);
        Inquiry inquiry = Inquiry.create(inquiryDto.getMessage(), inquiryDto.getInquiryCategory(), member);
        inquiryRepository.save(inquiry);
    }

}

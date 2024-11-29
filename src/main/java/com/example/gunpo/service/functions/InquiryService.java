package com.example.gunpo.service.functions;


import com.example.gunpo.domain.Inquiry;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.InquiryDto;
import com.example.gunpo.repository.InquiryRepository;
import com.example.gunpo.service.member.AuthenticationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@RequiredArgsConstructor
@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final AuthenticationService authenticationService;

    public void saveInquiry(InquiryDto inquiryDto, String accessToken) {
        Member member = getMember(accessToken);
        Inquiry inquiry = Inquiry.create(inquiryDto.getTitle(), inquiryDto.getMessage(),
                inquiryDto.getInquiryCategory(), member);
        inquiryRepository.save(inquiry);
    }

    public List<InquiryDto> getInquiryList(String acccessToken) {
        Member member = getMember(acccessToken);
        List<Inquiry> inquiryList = inquiryRepository.findByMember(member);
        return EntityToDto(inquiryList);
    }

    private static List<InquiryDto> EntityToDto(List<Inquiry> inquiryList) {
        return inquiryList.stream()
                .map(inquiry -> new InquiryDto(
                        inquiry.getTitle(),
                        inquiry.getMessage(),
                        inquiry.getCategory(),
                        LocalDateTime.now()
                )).toList();
    }

    private Member getMember(String accessToken) {
        return authenticationService.getUserDetails(accessToken);
    }

}

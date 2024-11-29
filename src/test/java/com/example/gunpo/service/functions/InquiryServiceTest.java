package com.example.gunpo.service.functions;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.example.gunpo.domain.Inquiry;
import com.example.gunpo.domain.InquiryCategory;
import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.functions.InquiryDto;
import com.example.gunpo.repository.InquiryRepository;
import com.example.gunpo.service.member.AuthenticationService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private InquiryService inquiryService;

    @BeforeEach
    void setup() {
        // Mock 초기화
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("문의가 잘 저장되는지 확인")
    void saveInquiry() {
        // Given
        String mockAccessToken = "mock-access-token";
        InquiryDto inquiryDto = new InquiryDto("로그인 문제좀 해결해주세요", "로그인이 이상해요", InquiryCategory.LOGIN_ISSUE,LocalDateTime.now());

        Member mockMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("TestUser")
                .build();

        Inquiry mockInquiry = Inquiry.builder()
                .id(1L)
                .message("로그인이 이상해요")
                .category(InquiryCategory.LOGIN_ISSUE)
                .member(mockMember)
                .build();

        // Mock 동작 설정
        when(authenticationService.getUserDetails(mockAccessToken)).thenReturn(mockMember);
        when(inquiryRepository.save(any(Inquiry.class))).thenReturn(mockInquiry);
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(mockInquiry));

        // When
        inquiryService.saveInquiry(inquiryDto, mockAccessToken);

        // Then
        Inquiry savedInquiry = inquiryRepository.findById(1L).orElse(null);
        assertThat(savedInquiry).isNotNull();
        assertThat(savedInquiry.getMessage()).isEqualTo("로그인이 이상해요");
        assertThat(savedInquiry.getCategory()).isEqualTo(InquiryCategory.LOGIN_ISSUE);

        // 메서드 호출 검증
        verify(authenticationService, times(1)).getUserDetails(mockAccessToken);
        verify(inquiryRepository, times(1)).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("회원의 문의 리스트를 성공적으로 가져오는지 확인")
    void getInquiryList() {
        // Given
        String mockAccessToken = "mock-access-token";

        Member mockMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("TestUser")
                .build();

        Inquiry inquiry1 = Inquiry.builder()
                .id(1L)
                .title("문의 1")
                .message("첫 번째 문의 메시지")
                .category(InquiryCategory.LOGIN_ISSUE)
                .createdAt(LocalDateTime.now())
                .member(mockMember)
                .build();

        Inquiry inquiry2 = Inquiry.builder()
                .id(2L)
                .title("문의 2")
                .message("두 번째 문의 메시지")
                .category(InquiryCategory.OTHER)
                .createdAt(LocalDateTime.now())
                .member(mockMember)
                .build();

        List<Inquiry> mockInquiryList = Arrays.asList(inquiry1, inquiry2);

        // Mock 동작 설정
        when(authenticationService.getUserDetails(mockAccessToken)).thenReturn(mockMember);
        when(inquiryRepository.findByMember(mockMember)).thenReturn(mockInquiryList);

        // When
        List<InquiryDto> inquiryDtos = inquiryService.getInquiryList(mockAccessToken);

        // Then
        assertThat(inquiryDtos).isNotNull();
        assertThat(inquiryDtos.size()).isEqualTo(2);

        InquiryDto inquiryDto1 = inquiryDtos.get(0);
        assertThat(inquiryDto1.getTitle()).isEqualTo("문의 1");
        assertThat(inquiryDto1.getMessage()).isEqualTo("첫 번째 문의 메시지");
        assertThat(inquiryDto1.getInquiryCategory()).isEqualTo(InquiryCategory.LOGIN_ISSUE);

        InquiryDto inquiryDto2 = inquiryDtos.get(1);
        assertThat(inquiryDto2.getTitle()).isEqualTo("문의 2");
        assertThat(inquiryDto2.getMessage()).isEqualTo("두 번째 문의 메시지");
        assertThat(inquiryDto2.getInquiryCategory()).isEqualTo(InquiryCategory.OTHER);

        // 메서드 호출 검증
        verify(authenticationService, times(1)).getUserDetails(mockAccessToken);
        verify(inquiryRepository, times(1)).findByMember(mockMember);
    }

}

package com.example.gunpo.controller.restapi;

import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.functions.InquiryDto;
import com.example.gunpo.service.functions.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ContactApiController {

    private final InquiryService inquiryService;

    private static final String INQUIRY_SAVE_SUCCESS = "성공적으로 문의를 완료했습니다.";

    @PostMapping("inquiry")
    public ResponseEntity<ResponseDto<?>> saveInquiry(
            @RequestBody InquiryDto inquiryDto,
            @CookieValue(value = "accessToken", required = false) String accessToken) {

        inquiryService.saveInquiry(inquiryDto, accessToken);
        return ResponseEntity.ok(new ResponseDto<>(INQUIRY_SAVE_SUCCESS, null, true));
    }

}

package com.example.gunpo.controller.view;

import com.example.gunpo.dto.functions.InquiryDto;
import com.example.gunpo.exception.member.UnauthorizedException;
import com.example.gunpo.service.functions.InquiryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ContactController {

    private final InquiryService inquiryService;

    @GetMapping("/contact")
    public String contactPage() {
        return "contact";
    }

    @GetMapping("/inquiry-list")
    public String getInquiryList(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            Model model
    ) {
        List<InquiryDto> inquiryList = inquiryService.getInquiryList(accessToken);
        model.addAttribute("inquiryDtoList", inquiryList);
        return "inquirylist";
    }

}

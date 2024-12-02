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
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (accessToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        try {
            List<InquiryDto> inquiryList = inquiryService.getInquiryList(accessToken);
            model.addAttribute("inquiryDtoList", inquiryList);
            return "inquirylist";
        } catch (UnauthorizedException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "문의하기는 로그인이 필요합니다.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "알 수 없는 오류가 발생했습니다.");
            return "redirect:/error";
        }
    }

}

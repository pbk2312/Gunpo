package com.example.gunpo.controller.view;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.MemberUpdateDto;
import com.example.gunpo.mapper.MemberMapper;
import com.example.gunpo.service.member.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MemberViewController {

    private final AuthenticationService authenticationService;


    @GetMapping("/sign-up")
    public String sign_up(@ModelAttribute MemberDto memberDto) {

        return "member/signup";

    }

    @GetMapping("/login")
    public String login(@ModelAttribute LoginDto loginDto, HttpServletRequest request) {
        // 사용자가 로그인 전에 접근한 URL을 저장
        String referer = request.getHeader("Referer");
        if (referer != null) {
            request.getSession().setAttribute("redirectUrl", referer);
        }
        return "member/login";
    }


    @GetMapping("/update")
    public String update(@CookieValue(value = "accessToken", required = false) String accessToken,
                         Model model) {
        Member member = authenticationService.getUserDetails(accessToken);

        MemberUpdateDto memberDto = getMemberUpdateDto(member);

        String formattedDate = getFormattedDate(member);

        model.addAttribute("memberDto", memberDto);
        model.addAttribute("formattedDateOfBirth", formattedDate);
        return "member/update";
    }

    private static MemberUpdateDto getMemberUpdateDto(Member member) {
        MemberUpdateDto memberDto = new MemberUpdateDto();
        memberDto.setId(member.getId());
        memberDto.setNickname(member.getNickname());
        memberDto.setDateOfBirth(member.getDateOfBirth());
        return memberDto;
    }

    private static String getFormattedDate(Member member) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = member.getDateOfBirth() != null
                ? member.getDateOfBirth().format(formatter)
                : "";
        return formattedDate;
    }

}

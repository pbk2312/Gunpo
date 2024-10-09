package com.example.gunpo.controller.restapi;


import com.example.gunpo.domain.Member;
import com.example.gunpo.dto.LoginDto;
import com.example.gunpo.dto.MemberDto;
import com.example.gunpo.dto.ResponseDto;
import com.example.gunpo.dto.TokenDto;
import com.example.gunpo.exception.IncorrectPasswordException;
import com.example.gunpo.exception.MemberNotFoundException;
import com.example.gunpo.exception.email.VerificationCodeMismatchException;
import com.example.gunpo.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberApiController{

    private final MemberService memberService;
    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto<?>> signUp(@RequestBody MemberDto memberDto) {
        try {
            Long saveMemberId = memberService.save(memberDto);
            log.info("저장된 회원 아이디: {}", saveMemberId);
            // 성공적인 응답 반환
            ResponseDto<Long> response = new ResponseDto<>("회원가입이 성공적으로 완료되었습니다.", saveMemberId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // 유효성 검사나 이메일 중복 시 에러 응답
            log.error("회원가입 오류: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        } catch (VerificationCodeMismatchException e) {
            // 인증 실패 시 에러 응답
            log.error("인증 오류: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>(e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (IncorrectPasswordException e) {
            log.error("비밀번호 불일치: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("입력한 비밀번호가 일치하지 않습니다.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        catch (Exception e) {
            // 기타 예외 처리
            log.error("회원가입 처리 중 예외 발생: {}", e.getMessage());
            ResponseDto<String> response = new ResponseDto<>("회원가입 처리 중 오류가 발생했습니다.", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseDto<?>> login(@RequestBody LoginDto loginDto,
                                                HttpServletResponse response,
                                                HttpServletRequest request) {
        try {
            TokenDto tokenDto = memberService.login(loginDto);

            addCookie(response, "accessToken", tokenDto.getAccessToken(), 3600); // 한시간
            addCookie(response, "refreshToken", tokenDto.getRefreshToken(), 36000); // 7일
            ResponseDto<Member> httpresponse = new ResponseDto<>("로그인 성공", null);
            return ResponseEntity.ok(httpresponse);
        } catch (MemberNotFoundException e) {
            // MemberNotFoundException 발생 시, 구체적인 메시지 전달
            return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IncorrectPasswordException e) {
            // IncorrectPasswordException 발생 시, 구체적인 메시지 전달
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            // 기타 예외 발생 시, 구체적인 메시지 전달
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private ResponseEntity<ResponseDto<?>> buildErrorResponse(HttpStatus status, String message) {
        ResponseDto<?> response = new ResponseDto<>(message, null);
        return ResponseEntity.status(status).body(response);
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = createCookie(name, value, maxAge);
        response.addCookie(cookie);
    }

    private void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = createCookie(name, null, 0);
        response.addCookie(cookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true); // https 환경에서 사용하려면 true로
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

}

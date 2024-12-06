package com.example.gunpo.dto.member.oauth;

import com.example.gunpo.domain.Member;
import com.example.gunpo.domain.MemberRole;
import com.example.gunpo.exception.member.UnsupportedProviderException;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Builder
@Getter
@ToString
@SuppressWarnings("unchecked")
public class OAuth2UserInfo {

    private String id;
    private String password;
    private String email;
    private String nickname;
    private String provider;

    public static OAuth2UserInfo of(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw new UnsupportedProviderException("Unsupported provider: " + provider);
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .provider("google")
                .id("google_" + (String) attributes.get("sub"))
                .password((String) attributes.get("sub")) // 구글은 ID를 password로 사용
                .nickname((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }

    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = (String) kakaoAccount.get("email");
        return OAuth2UserInfo.builder()
                .provider("kakao")
                .id("kakao_" + attributes.get("id").toString())
                .password(attributes.get("id").toString())
                .nickname((String) properties.get("nickname"))
                .email(email) // 이메일 추가
                .build();
    }

    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2UserInfo.builder()
                .provider("naver")
                .id("naver_" + (String) response.get("id"))
                .password((String) response.get("id")) // 네이버는 ID를 password로 사용
                .nickname((String) response.get("name"))
                .email((String) response.get("email")) // 이메일 추가
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password) // 소셜 로그인에서 제공하는 ID를 password로 사용
                .provider(provider)
                .nickname(nickname)
                .memberRole(MemberRole.MEMBER) // 기본 역할을 MEMBER로 설정
                .neighborhoodVerification(false) // 기본값: false
                .build();
    }

}
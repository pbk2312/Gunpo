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
                .password((String) attributes.get("sub"))
                .nickname((String) attributes.get("name") + "_google")
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
                .nickname((String) properties.get("nickname") + "_kakao")
                .email(email)
                .build();
    }

    private static OAuth2UserInfo ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuth2UserInfo.builder()
                .provider("naver")
                .id("naver_" + (String) response.get("id"))
                .password((String) response.get("id"))
                .nickname((String) response.get("name") + "_naver")
                .email((String) response.get("email"))
                .build();
    }

    public Member toEntity() {
        return Member.builder()
                .email(email)
                .password(password)
                .provider(provider)
                .nickname(nickname)
                .memberRole(MemberRole.MEMBER)
                .neighborhoodVerification(false)
                .build();
    }

}

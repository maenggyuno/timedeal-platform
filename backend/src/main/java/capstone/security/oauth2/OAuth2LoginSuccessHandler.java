//package capstone.security.oauth2;
//
//import capstone.domain.User;
//import capstone.repository.LoginJpaRepository;
//import capstone.security.jwt.JwtTokenProvider;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Map;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//@PropertySource("classpath:application-login.properties")
//public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final LoginJpaRepository loginJpaRepository;
//
//    @Value("${jwt.cookie.name}")
//    private String jwtCookieName;
//
//    @Value("${jwt.expiration.ms}")
//    private long tokenValidityInMilliseconds;
//
//    @Value("${jwt.refresh.expiration.ms}")
//    private long refreshTokenValidityInMilliseconds;
//
//    @Value("${frontend.url}")
//    private String frontendUrl;
//
//    @Override
//    @Transactional
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
//            throws ServletException, IOException {
//
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//
//        String provider = oauthToken.getAuthorizedClientRegistrationId();
//        String socialLoginId = extractSocialLoginId(oAuth2User, provider);
//
//        if (socialLoginId == null) {
//            log.error("Failed to extract socialLoginId for provider: {}. Attributes: {}", provider, oAuth2User.getAttributes());
//            getRedirectStrategy().sendRedirect(request, response, "/login/failure?error=socialId_extraction_failed");
//            return;
//        }
//
//        // 사용자 조회 또는 신규 생성
//        User user = loginJpaRepository.findBySocialLoginId(socialLoginId)
//                .orElseGet(() -> createNewUser(oAuth2User, socialLoginId, provider));
//
//        // 1. Access Token과 Refresh Token 생성
//        String accessToken = jwtTokenProvider.createToken(user);
//        String refreshToken = jwtTokenProvider.createRefreshToken(user);
//
//        // 2. DB에 새로운 Refresh Token 저장 (덮어쓰기)
//        user.updateRefreshToken(refreshToken);
//        loginJpaRepository.save(user);
//        log.info("User {}'s refresh token has been updated in the database.", user.getUserId());
//
//        // 3. Access Token을 위한 쿠키 생성 및 설정
//        Cookie accessTokenCookie = new Cookie(jwtCookieName, accessToken);
//        accessTokenCookie.setHttpOnly(true);
//        accessTokenCookie.setSecure(false); // 로컬 개발 환경에서는 false, 프로덕션에서는 true
//        accessTokenCookie.setPath("/");
//        accessTokenCookie.setMaxAge((int) (tokenValidityInMilliseconds / 1000));
//        response.addCookie(accessTokenCookie);
//
//        // 4. Refresh Token을 위한 쿠키 생성 및 설정
//        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
//        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(false); // 로컬 개발 환경에서는 false, 프로덕션에서는 true
//        refreshTokenCookie.setPath("/api/auth/refresh"); // 토큰 재발급을 요청하는 경로에서만 사용되도록 제한
//        refreshTokenCookie.setMaxAge((int) (refreshTokenValidityInMilliseconds / 1000));
//        response.addCookie(refreshTokenCookie);
//
//        // 5. 프론트엔드의 콜백 페이지로 리다이렉션
//        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
//                .build().encode(StandardCharsets.UTF_8).toUriString();
//
//        log.info("Authentication successful. Redirecting to frontend callback: {}", targetUrl);
//        getRedirectStrategy().sendRedirect(request, response, targetUrl);
//    }
//
//    /**
//     * OAuth2 공급자로부터 받은 사용자 정보에서 고유 ID를 추출합니다.
//     */
//    private String extractSocialLoginId(OAuth2User oAuth2User, String provider) {
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        log.debug("OAuth2 attributes for provider {}: {}", provider, attributes);
//
//        switch (provider.toLowerCase()) {
//            case "google":
//                return (String) attributes.get("sub");
//            case "naver":
//                if (attributes.get("response") instanceof Map) {
//                    @SuppressWarnings("unchecked")
//                    Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
//                    return (String) responseMap.get("id");
//                }
//                return null;
//            default:
//                log.error("Unsupported OAuth2 provider: {}", provider);
//                return null;
//        }
//    }
//
//    /**
//     * 새로운 사용자를 생성하고 DB에 저장합니다.
//     */
//    private User createNewUser(OAuth2User oAuth2User, String socialLoginId, String provider) {
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//        String email;
//        String name;
//
//        if ("naver".equalsIgnoreCase(provider) && attributes.get("response") instanceof Map) {
//            @SuppressWarnings("unchecked")
//            Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
//            email = (String) responseMap.get("email");
//            name = (String) responseMap.get("name");
//        } else { // Google 및 기타
//            email = (String) attributes.get("email");
//            name = (String) attributes.get("name");
//        }
//
//        if (email == null) {
//            log.warn("Email not provided by {}. Setting a placeholder.", provider);
//            email = socialLoginId + "@" + provider + ".local";
//        }
//
//        log.info("Creating a new user. socialLoginId={}, provider={}, email={}", socialLoginId, provider, email);
//
//        User newUser = User.builder()
//                .socialLoginPlatform(provider)
//                .socialLoginId(socialLoginId)
//                .email(email)
//                .name(name)
//                .build();
//
//        return loginJpaRepository.save(newUser);
//    }
//}

package capstone.security.oauth2;

import capstone.domain.User;
import capstone.repository.LoginJpaRepository;
import capstone.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final LoginJpaRepository loginJpaRepository;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.expiration.ms}")
    private long tokenValidityInMilliseconds;

    @Value("${jwt.refresh.expiration.ms}")
    private long refreshTokenValidityInMilliseconds;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${cookie.secure}")
    private boolean isCookieSecure;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {

        log.info(">>>>> OAuth2 Login Success! Handler execution started. <<<<<");

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        String socialLoginId = extractSocialLoginId(oAuth2User, provider);

        if (socialLoginId == null) {
            log.error("Failed to extract socialLoginId for provider: {}. Attributes: {}", provider, oAuth2User.getAttributes());
            getRedirectStrategy().sendRedirect(request, response, "/login/failure?error=socialId_extraction_failed");
            return;
        }

        // 사용자 조회 또는 신규 생성
        User user = loginJpaRepository.findBySocialLoginId(socialLoginId)
                .orElseGet(() -> createNewUser(oAuth2User, socialLoginId, provider));

        // 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        // DB에 Refresh Token 저장
        user.updateRefreshToken(refreshToken);
        loginJpaRepository.save(user);
        log.info("User {}'s refresh token has been updated in the database.", user.getUserId());

        // Access Token 쿠키 생성
        ResponseCookie accessTokenCookie = ResponseCookie.from(jwtCookieName, accessToken)
                .httpOnly(true)
//                .secure(false)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(tokenValidityInMilliseconds / 1000)
                .sameSite("Lax") // CSRF 공격 방지를 위한 설정
                .build();

        // 응답 헤더에 쿠키 추가
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        log.info("Access Token cookie has been set in the response header.");

        // Refresh Token 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
//                .secure(false)
                .secure(isCookieSecure)
                .path("/")
                .maxAge(refreshTokenValidityInMilliseconds / 1000)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        log.info("Refresh Token cookie has been set in the response header.");


        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .build().encode(StandardCharsets.UTF_8).toUriString();

        log.info("All cookies processed. Redirecting to: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * OAuth2 공급자로부터 받은 사용자 정보에서 고유 ID를 추출
     */
    private String extractSocialLoginId(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        log.debug("OAuth2 attributes for provider {}: {}", provider, attributes);

        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "naver":
                if (attributes.get("response") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
                    return (String) responseMap.get("id");
                }
                return null;
            default:
                log.error("Unsupported OAuth2 provider: {}", provider);
                return null;
        }
    }

    /**
     * 새로운 사용자를 생성하고 DB에 저장
     */
    private User createNewUser(OAuth2User oAuth2User, String socialLoginId, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email;
        String name;

        if ("naver".equalsIgnoreCase(provider) && attributes.get("response") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = (Map<String, Object>) attributes.get("response");
            email = (String) responseMap.get("email");
            name = (String) responseMap.get("name");
        } else { // Google
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }

        if (email == null) {
            log.warn("Email not provided by {}. Setting a placeholder.", provider);
            email = socialLoginId + "@" + provider + ".local";
        }

        log.info("Creating a new user. socialLoginId={}, provider={}, email={}", socialLoginId, provider, email);

        User newUser = User.builder()
                .socialLoginPlatform(provider)
                .socialLoginId(socialLoginId)
                .email(email)
                .name(name)
                .build();

        return loginJpaRepository.save(newUser);
    }
}

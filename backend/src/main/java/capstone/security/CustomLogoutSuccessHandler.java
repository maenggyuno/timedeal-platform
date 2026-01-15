package capstone.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@PropertySource("classpath:application-login.properties")
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        log.info("CustomLogoutSuccessHandler: Invalidating cookies for logout.");

        // access_token 쿠키 삭제
        Cookie accessTokenCookie = new Cookie(jwtCookieName, null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        // refresh_token 쿠키 삭제
        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        // 성공 상태 응답
        response.setStatus(HttpStatus.OK.value());
        log.info("Successfully processed logout.");
    }
}
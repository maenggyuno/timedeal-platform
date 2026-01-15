package capstone.controller;

import capstone.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.expiration.ms}")
    private long tokenValidityInMilliseconds;

    @PostMapping("/api/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing.");
        }

        try {
            String newAccessToken = tokenService.refreshAccessToken(refreshToken);

            Cookie newAccessTokenCookie = new Cookie(jwtCookieName, newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setSecure(false);
            newAccessTokenCookie.setPath("/");
            newAccessTokenCookie.setMaxAge((int) (tokenValidityInMilliseconds / 1000));
            response.addCookie(newAccessTokenCookie);

            return ResponseEntity.ok().body("Access token refreshed successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

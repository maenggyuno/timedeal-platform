package capstone.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        log.error("OAuth2 Login Failed: {}", exception.getMessage(), exception);

        // 실패 시 리다이렉트할 프론트엔드 URL 설정
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/login/failure")
                .queryParam("error", exception.getLocalizedMessage())
                .build().encode(StandardCharsets.UTF_8).toUriString();

        setDefaultFailureUrl(targetUrl);
        super.onAuthenticationFailure(request, response, exception);
    }
}
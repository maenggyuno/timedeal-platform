package capstone.service;

import capstone.domain.User;
import capstone.repository.LoginJpaRepository;
import capstone.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final LoginJpaRepository loginJpaRepository;

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token.");
        }

        // 2. Token에서 User ID 추출
        Long userId = Long.valueOf(jwtTokenProvider.getAuthentication(refreshToken).getName());

        // 3. DB에서 User 조회 및 Refresh Token 일치 여부 확인
        User user = loginJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (!refreshToken.equals(user.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token mismatch.");
        }

        // 4. 새로운 Access Token 생성
        return jwtTokenProvider.createToken(user);
    }
}

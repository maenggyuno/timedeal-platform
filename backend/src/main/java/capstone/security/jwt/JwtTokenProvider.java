package capstone.security.jwt;

import capstone.domain.User;
import capstone.security.dto.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyPlain;

    @Value("${jwt.expiration.ms}")
    private Long tokenValidityInMilliseconds;

    @Value("${jwt.cookie.name}")
    private String jwtCookieName;

    @Value("${jwt.refresh.expiration.ms}")
    private Long refreshTokenValidityInMilliseconds;

    private SecretKey key;
    private JwtParser jwtParser;
    private MacAlgorithm signatureAlgorithm = Jwts.SIG.HS256;

    @PostConstruct
    protected void init() {
        // secretKeyPlain 문자열로부터 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(secretKeyPlain.getBytes());

        // JwtParser 인스턴스 생성 및 초기화 (0.11.0+ 방식)
        // 이 JwtParser는 서명 키 검증을 포함하여 설정됨
        this.jwtParser = Jwts.parser()
                .verifyWith(this.key)
                .build();
    }

    // name, email 정보 포함한 JWT Token 생성
    public String createToken(User user) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("email", user.getEmail());
        claimsMap.put("name", user.getName());

        Date now = new Date();
        Date validity = new Date(now.getTime() + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claims(claimsMap)
                .issuedAt(now)
                .expiration(validity)
                .signWith(this.key, this.signatureAlgorithm)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            log.warn("Could not get claims from token for authentication");
            return null;
        }
        Long userId = Long.parseLong(claims.getSubject());
        String email = (String) claims.get("email");

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(new String[]{"ROLE_USER"})
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserPrincipal principal = UserPrincipal.create(userId, email);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    // 유효한 토큰인지 확인
    public boolean validateToken(String jwtToken) {
        try {
            this.jwtParser.parseSignedClaims(jwtToken); // Jws<Claims> 반환, 유효하지 않으면 예외 발생
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or null: {}", e.getMessage());
        }
        return false;
    }

    private Claims parseClaims(String token) {
        try {
            return this.jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Parsing claims from an expired JWT token: {}", e.getMessage());
            return e.getClaims();
        } catch (Exception e) {
            log.warn("Error parsing JWT claims: {}, Token: [{}]", e.getMessage(), tokenLogTrimming(token));
            return null;
        }
    }

    private String tokenLogTrimming(String token) {
        if (token == null) return null;
        return token.length() > 50 ? token.substring(0, 50) + "..." : token;
    }

    public String getJwtCookieName() {
        if (this.jwtCookieName == null || this.jwtCookieName.trim().isEmpty()) {
            log.error("JWT cookie name is not configured properly in properties file (jwt.cookie.name).");
            return "AUTH_TOKEN";
        }
        return jwtCookieName;
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + this.refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .issuedAt(now)
                .expiration(validity)
                .signWith(this.key, this.signatureAlgorithm)
                .compact();
    }
}

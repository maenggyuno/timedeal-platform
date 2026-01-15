package capstone.security.dto;

import capstone.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User; // ✅ OAuth2User import

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long userId;
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes; // ✅ OAuth2 속성을 담을 필드

    public UserPrincipal(Long userId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
    }

    public UserPrincipal(Long userId, String email, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.userId = userId;
        this.email = email;
        this.authorities = authorities;
        this.attributes = attributes;
    }

    // JWT 인증 시 사용할 메서드
    public static UserPrincipal create(Long userId, String email) {
        return new UserPrincipal(
                userId,
                email,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    // OAuth2 인증 시 사용할 메서드
    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getUserId(),
                user.getEmail(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                attributes
        );
        return userPrincipal;
    }

    // UserDetails 메서드 구현
    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return String.valueOf(this.userId); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    // OAuth2User 메서드 구현
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(this.userId);
    }
}
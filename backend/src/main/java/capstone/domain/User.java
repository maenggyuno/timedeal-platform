package capstone.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "social_login_platform", length = 50)
    private String socialLoginPlatform;

    @Column(name = "social_login_id", length = 100)
    private String socialLoginId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Builder
    public User(String socialLoginPlatform, String socialLoginId, String name, String email) {
        this.socialLoginPlatform = socialLoginPlatform;
        this.socialLoginId = socialLoginId;
        this.name = name;
        this.email = email;
    }

    // 정보 업데이트
    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }

    // Refresh Token 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

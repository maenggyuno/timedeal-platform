package capstone.repository;

import capstone.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialLoginPlatformAndSocialLoginId(String socialPlatform, String socialLoginId);
    Optional<User> findBySocialLoginId(String socialLoginId);
}

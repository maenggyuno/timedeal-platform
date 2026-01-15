package capstone.service;

import capstone.dto.login.response.UserInfoResponse;
import capstone.repository.LoginMySqlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("classpath:application-login.properties")
public class LoginService {

    @Value("${jwt.expiration.ms}")
    private Long tokenValidityInMilliseconds;

    private final LoginMySqlRepository repository;

    public LoginService(LoginMySqlRepository repository) {
        this.repository = repository;
    }

    public UserInfoResponse getUserInfo(String userId) {
        return repository.findUserInfo(userId);
    }
}

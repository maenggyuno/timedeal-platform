package capstone.dto.login.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserInfoResponse {

    private final String name;
    private final String email;
    private final String social_login_platform;
    private final List<Long> authorizedMarts;
}
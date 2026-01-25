package capstone.repository;

import capstone.dto.login.response.UserInfoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class LoginMySqlRepository {

    private final JdbcTemplate jdbcTemplate;

    public LoginMySqlRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserInfoResponse findUserInfo(String userId) {
        String userInfoSql = "SELECT name, email, social_login_platform FROM users WHERE user_id = ?";
        String martsSql = "SELECT store_id FROM store_employee WHERE user_id = ?";

        return jdbcTemplate.queryForObject(userInfoSql, (rs, rowNum) -> {
            String name = rs.getString("name");
            String email = rs.getString("email");
            String socialLoginPlatform = rs.getString("social_login_platform");

            Long userIdAsLong = Long.parseLong(userId);
            List<Long> martIds = jdbcTemplate.queryForList(martsSql, Long.class, userIdAsLong);

            return new UserInfoResponse(name, email, socialLoginPlatform, martIds);
        }, userId);
    }
}

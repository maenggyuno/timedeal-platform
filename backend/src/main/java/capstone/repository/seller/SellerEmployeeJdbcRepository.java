package capstone.repository.seller;

import capstone.controller.seller.SellerUserSearchResponse;
import capstone.dto.seller.response.SellerEmployeeListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SellerEmployeeJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SellerEmployeeListResponse> employeeListRowMapper = (rs, rowNum) -> new SellerEmployeeListResponse(
            rs.getLong("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getInt("authority") == 1 ? "총괄 관리자" : "직원" // 명칭 변경
    );

    private final RowMapper<SellerUserSearchResponse> userSearchRowMapper = (rs, rowNum) -> new SellerUserSearchResponse(
            rs.getLong("user_id"),
            rs.getString("name"),
            rs.getString("email"),
            null
    );

    public List<SellerEmployeeListResponse> findEmployeesByStoreId(Long storeId) {
        String sql = "SELECT u.user_id, u.name, u.email, se.authority " +
                "FROM users u JOIN storeEmployee se ON u.user_id = se.user_id " +
                "WHERE se.store_id = ?";
        return jdbcTemplate.query(sql, employeeListRowMapper, storeId);
    }

    /**
     * 총괄 관리자 권한을 위임하는 메서드 (두 직원의 권한을 교체)
     */
    public void delegateManagerAuthority(Long storeId, Long oldManagerId, Long newManagerId) {
        // 1. 기존 총괄 관리자를 직원으로 강등
        String sql1 = "UPDATE storeEmployee SET authority = 0 WHERE store_id = ? AND user_id = ?";
        jdbcTemplate.update(sql1, storeId, oldManagerId);

        // 2. 새로운 직원을 총괄 관리자로 승급
        String sql2 = "UPDATE storeEmployee SET authority = 1 WHERE store_id = ? AND user_id = ?";
        jdbcTemplate.update(sql2, storeId, newManagerId);
    }

    public void deleteEmployees(Long storeId, List<Long> userIds) {
        String sql = "DELETE FROM storeEmployee WHERE store_id = ? AND user_id = ?";
        jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
            ps.setLong(1, storeId);
            ps.setLong(2, userId);
        });
    }

    public Optional<SellerUserSearchResponse> findUserByEmail(String email) {
        String sql = "SELECT user_id, name, email FROM users WHERE email = ?";
        List<SellerUserSearchResponse> users = jdbcTemplate.query(sql, userSearchRowMapper, email);
        return users.stream().findFirst();
    }

    public boolean isAlreadyEmployee(Long storeId, Long userId) {
        String sql = "SELECT COUNT(*) FROM storeEmployee WHERE store_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, storeId, userId);
        return count != null && count > 0;
    }

    public void addEmployees(Long storeId, List<Long> userIds) {
        String sql = "INSERT INTO storeEmployee (store_id, user_id, authority) VALUES (?, ?, 0)";
        jdbcTemplate.batchUpdate(sql, userIds, 100, (ps, userId) -> {
            ps.setLong(1, storeId);
            ps.setLong(2, userId);
        });
    }
}


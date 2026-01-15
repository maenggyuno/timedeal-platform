package capstone.repository.seller;

import capstone.domain.StoreEmployee;
import capstone.domain.StoreEmployeeId;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SellerStoreJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void save(StoreEmployee storeEmployee) {
        String sql = "INSERT INTO storeEmployee (store_id, user_id, authority) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql,
                storeEmployee.getId().getStoreId(),
                storeEmployee.getId().getUserId(),
                storeEmployee.getAuthority()
        );
    }

    public List<StoreEmployee> findById_UserId(Long userId) {
        String sql = "SELECT store_id, user_id, authority FROM storeEmployee WHERE user_id = ?";

        return jdbcTemplate.query(sql, new RowMapper<StoreEmployee>() {
            @Override
            public StoreEmployee mapRow(ResultSet rs, int rowNum) throws SQLException {
                StoreEmployeeId id = new StoreEmployeeId(
                        rs.getLong("store_id"),
                        rs.getLong("user_id")
                );
                return StoreEmployee.builder()
                        .id(id)
                        .authority(rs.getInt("authority"))
                        .build();
            }
        }, userId);
    }
}

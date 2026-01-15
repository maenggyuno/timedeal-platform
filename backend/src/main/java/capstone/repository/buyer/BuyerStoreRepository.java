package capstone.repository.buyer;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BuyerStoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuyerStoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void followStore(Long userId, Long storeId) {
        String sql = "INSERT INTO follows(user_id, store_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, storeId);
    }

    public void unfollowStore(Long userId, Long storeId) {
        String sql = "DELETE FROM follows WHERE user_id = ? AND store_id = ?";
        jdbcTemplate.update(sql, userId, storeId);
    }
}

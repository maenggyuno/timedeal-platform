package capstone.repository.buyer;

import capstone.dto.buyer.response.ReviewResponse;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class BuyerReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuyerReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 리뷰 저장
    public void save(Long orderId, Long userId, int rating, String content) {
        String sql = "INSERT INTO reviews (order_id, user_id, rating, content, created_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        // created_at에 현재 시간을 바로 입력
        jdbcTemplate.update(sql, orderId, userId, rating, content, LocalDateTime.now());
    }

    // orderId로 리뷰 조회
    public Optional<ReviewResponse> findByOrderId(Long orderId) {
        String sql = "SELECT rating, content, created_at " +
                "FROM reviews WHERE order_id = ?";
        try {
            ReviewResponse review = jdbcTemplate.queryForObject(sql, new RowMapper<ReviewResponse>() {
                @Override
                public ReviewResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new ReviewResponse(
                            rs.getInt("rating"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                }
            }, orderId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}

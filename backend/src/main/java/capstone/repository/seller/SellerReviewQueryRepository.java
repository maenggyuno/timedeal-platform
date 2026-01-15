// src/main/java/capstone/repository/seller/ReviewQueryRepository.java
package capstone.repository.seller;

import capstone.dto.seller.SellerReviewDto;
import capstone.dto.seller.SellerReviewSummaryDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SellerReviewQueryRepository {
  private final JdbcTemplate jdbc;
  public SellerReviewQueryRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  public List<SellerReviewDto> findReviewsByStoreId(long storeId) {
    String sql = """
        SELECT
          r.review_id  AS reviewId,
          CONCAT('user#', r.user_id) AS userName,
          r.rating     AS stars,
          r.content    AS text,
          r.created_at AS createdAt
        FROM reviews r
        JOIN `orderItems` oi ON oi.order_id = r.order_id
        WHERE oi.seller_id = ?
        ORDER BY r.created_at DESC
        """;
    return jdbc.query(sql, (rs, i) -> new SellerReviewDto(
        rs.getLong("reviewId"),
        rs.getString("userName"),
        rs.getInt("stars"),
        rs.getString("text"),
        rs.getTimestamp("createdAt").toLocalDateTime()
    ), storeId);
  }

  public SellerReviewSummaryDto getSummary(long storeId) {
    String sql = """
        SELECT
        ROUND(AVG(r.rating), 1)                                         AS avgAll,
        ROUND(AVG(CASE WHEN r.created_at >= DATE_SUB(NOW(), INTERVAL 3 MONTH) THEN r.rating END), 1) AS avg3m,
        COUNT(*)                                                        AS cntAll,
        SUM(r.created_at >= DATE_SUB(NOW(), INTERVAL 3 MONTH))          AS cnt3m
        FROM reviews r
        JOIN `orderItems` oi ON oi.order_id = r.order_id
        WHERE oi.seller_id = ?
    """;
    return jdbc.queryForObject(sql, (rs, i) -> new SellerReviewSummaryDto(
        rs.getBigDecimal("avgAll") == null ? null : rs.getBigDecimal("avgAll").doubleValue(),
        rs.getBigDecimal("avg3m") == null ? null : rs.getBigDecimal("avg3m").doubleValue(),
        rs.getInt("cntAll"),
        rs.getInt("cnt3m")
    ), storeId);
  }
}



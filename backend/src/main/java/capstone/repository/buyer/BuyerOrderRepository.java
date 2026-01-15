package capstone.repository.buyer;

import capstone.dto.buyer.response.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BuyerOrderRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BuyerOrderRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     *  단일상품 주문완료시 출력
     * */
    public List<BuyerOrderCompleteResponse> getOrderComplete(Long orderId) {
        String sql = "SELECT p.product_img_src, p.product_name, oi.quantity, p.price, " +
                "(p.price * oi.quantity) AS total_price, q.valid_until, oi.status " +
                "FROM orderItems oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN qrCodes q ON oi.order_id = q.order_id " +
                "WHERE oi.order_id = ?";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(BuyerOrderCompleteResponse.class), orderId);
    }

    /**
     *  여러상품 주문완료시 출력 (ex. 장바구니 결제)
     * */
    public List<BuyerOrderCompleteResponse> findOrdersByIds(List<Long> orderIds) {
        String sql = "SELECT oi.order_id, p.product_img_src, p.product_name, oi.quantity, p.price, " +
                "oi.total_price, q.valid_until, oi.status " +
                "FROM orderItems oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN qrCodes q ON oi.order_id = q.order_id " +
                "WHERE oi.order_id IN (:orderIds)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("orderIds", orderIds);

        return namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(BuyerOrderCompleteResponse.class));
    }


    /**
     *  구매 중인 상품 출력
     */
    public List<BuyerBuyingResponse> getBuying(Long userId) {
        String sql = "SELECT " +
                "    p.product_id, p.product_img_src, p.product_name, " +
                "    oi.order_id, oi.quantity, oi.total_price, oi.status, " +
                "    q.valid_until, " +
                "    s.store_id, s.name, s.address " +
                "FROM orderItems oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN stores s ON oi.seller_id = s.store_id " +
                "JOIN qrCodes q ON oi.order_id = q.order_id " +
                "WHERE oi.buyer_id = ? " +
                "AND oi.status IN (1, 2, 3)";

        return jdbcTemplate.query(sql, new RowMapper<BuyerBuyingResponse>() {
            @Override
            public BuyerBuyingResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                int status = rs.getInt("status");
                String credit_method;
                if (status == 1) {
                    credit_method = "현장결제 필요";
                } else {
                    credit_method = "카드결제 완료";
                }

                return new BuyerBuyingResponse(
                        rs.getLong("order_id"),
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getLong("total_price"),
                        rs.getTimestamp("valid_until").toLocalDateTime(),
                        rs.getLong("store_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        credit_method
                );
            }
        }, userId);
    }

    /**
     *  QR코드 정보 + 가게정보 조회
     * */
    public Optional<BuyerQrcodeExtensionResponse> findQrCodeDetailsByOrderId(Long orderId) {
        String sql = "SELECT q.count, q.valid_until, s.closing_time, s.opening_time " +
                "FROM qrCodes q " +
                "JOIN orderItems oi ON q.order_id = oi.order_id " +
                "JOIN stores s ON oi.seller_id = s.store_id " +
                "WHERE q.order_id = ?";
        try {
            BuyerQrcodeExtensionResponse result = jdbcTemplate.queryForObject(sql, new Object[]{orderId}, (rs, rowNum) -> {

                        // KST 변환
                        java.sql.Timestamp validUntilTimestamp = rs.getTimestamp("valid_until");
                        LocalDateTime validUntilKST = validUntilTimestamp.toInstant()
                                .atZone(java.time.ZoneId.of("Asia/Seoul"))
                                .toLocalDateTime();

                        return new BuyerQrcodeExtensionResponse(
                                rs.getInt("count"),
                                validUntilKST,
                                rs.getTime("closing_time").toLocalTime(),
                                rs.getTime("opening_time").toLocalTime()
                        );
                    }
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // 구매가능시간 연장
    public int updateQrCode(Long orderId, LocalDateTime newValidUntil, int newCount) {
        String sql = "UPDATE qrCodes SET valid_until = ?, count = ? WHERE order_id = ?";
        return jdbcTemplate.update(sql, newValidUntil, newCount, orderId);
    }

    /**
     *  구매중인 상품 구매 취소
     */
    // 주문 ID로 상품 ID와 수량을 조회
    public Optional<BuyerOrderCancelInfoResponse> findOrderInfoById(Long orderId) {
        String sql = "SELECT product_id, quantity FROM orderItems WHERE order_id = ?";
        try {
            BuyerOrderCancelInfoResponse orderInfo = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new BuyerOrderCancelInfoResponse(
                    rs.getLong("product_id"),
                    rs.getInt("quantity")
            ), orderId);
            return Optional.ofNullable(orderInfo);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // 상품의 재고 수량을 증가시킴
    public int increaseProductQuantity(Long productId, int quantity) {
        String sql = "UPDATE products SET quantity = quantity + ? WHERE product_id = ?";
        return jdbcTemplate.update(sql, quantity, productId);
    }

    // 주문 ID에 해당하는 QR 코드를 삭제
    public int deleteQrCodeByOrderId(Long orderId) {
        String sql = "DELETE FROM qrCodes WHERE order_id = ?";
        return jdbcTemplate.update(sql, orderId);
    }

    // 주문 아이템의 상태를 변경
    public int updateOrderItemStatus(Long orderId, int status) {
        String sql = "UPDATE orderItems SET status = ? WHERE order_id = ?";
        return jdbcTemplate.update(sql, status, orderId);
    }

    // 특정 상품에 대해 활성 상태(수령 전)인 주문의 개수를 카운트
    public int countActiveOrdersByProductId(Long productId) {
        String sql = "SELECT COUNT(*) FROM orderItems WHERE product_id = ? AND status IN (1, 2, 3)";
        return jdbcTemplate.queryForObject(sql, Integer.class, productId);
    }

    //상품의 판매 상태를 변경
    public int updateProductStatus(Long productId, int status) {
        String sql = "UPDATE products SET status = ? WHERE product_id = ?";
        return jdbcTemplate.update(sql, productId, status);
    }

    /**
     *  구매 취소된 상품 출력
     * */
    public List<BuyerBuyCancelResponse> getBuyCancel(Long userId) {
        String sql = "SELECT " +
                "    p.product_id, p.product_img_src, p.product_name, " +
                "    oi.order_id, oi.quantity, oi.total_price, oi.status, " +
                "    s.store_id, s.name, s.address, s.is_deleted " +
                "FROM orderItems oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN stores s ON oi.seller_id = s.store_id " +
                "WHERE oi.buyer_id = ? " +
                "AND oi.status IN (0)";

        return jdbcTemplate.query(sql, new RowMapper<BuyerBuyCancelResponse>() {
            @Override
            public BuyerBuyCancelResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new BuyerBuyCancelResponse(
                        rs.getLong("order_id"),
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getLong("total_price"),
                        rs.getLong("store_id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getBoolean("is_deleted")
                );
            }
        }, userId);
    }

    /**
     *  구매 완료된 상품 출력
     * */
    public List<BuyerBuyCompletedResponse> getCompletedOrders(Long userId) {
        String sql = "SELECT " +
                "    oi.order_id, p.product_id, p.product_img_src, p.product_name, " +
                "    oi.quantity, oi.total_price, s.store_id, s.name AS storeName, s.address AS storeAddress, s.is_deleted AS storeIsDeleted, " +
                "    CASE WHEN r.review_id IS NOT NULL THEN TRUE ELSE FALSE END AS isReviewed, " +
                "    r.created_at AS reviewCreatedAt " +
                "FROM orderItems oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN stores s ON oi.seller_id = s.store_id " +
                "LEFT JOIN reviews r ON oi.order_id = r.order_id " +
                "WHERE oi.buyer_id = ? " +
                "AND oi.status = 4";

        return jdbcTemplate.query(sql, new RowMapper<BuyerBuyCompletedResponse>() {
            @Override
            public BuyerBuyCompletedResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
                java.sql.Timestamp createdAtTimestamp = rs.getTimestamp("reviewCreatedAt");
                LocalDateTime reviewCreatedAt = (createdAtTimestamp != null) ? createdAtTimestamp.toLocalDateTime() : null;

                return new BuyerBuyCompletedResponse(
                        rs.getLong("order_id"),
                        rs.getLong("product_id"),
                        rs.getString("product_img_src"),
                        rs.getString("product_name"),
                        rs.getInt("quantity"),
                        rs.getLong("total_price"),
                        rs.getLong("store_id"),
                        rs.getBoolean("storeIsDeleted"),
                        rs.getString("storeName"),
                        rs.getString("storeAddress"),
                        rs.getBoolean("isReviewed"),
                        reviewCreatedAt
                );
            }
        }, userId);
    }

    /**
     *  구매 가능 시간 만료되었지만, Status가 변경되지 않은 주문 조회
     * */
    public List<Long> findExpiredOrderIds(LocalDateTime currentTime) {
        String sql = "SELECT oi.order_id " +
                "FROM orderItems oi " +
                "JOIN qrCodes q ON oi.order_id = q.order_id " +
                "WHERE q.valid_until < ? " +
                "AND oi.status IN (1, 2, 3)";

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("order_id"), currentTime);
    }
}

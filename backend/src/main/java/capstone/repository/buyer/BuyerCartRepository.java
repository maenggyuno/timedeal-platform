package capstone.repository.buyer;

import capstone.dto.buyer.response.BuyerCartItemResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BuyerCartRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BuyerCartRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    // 장바구니 상품추가
    public void addToCart(Long userId, Long productId, Long quantity) {
        String sql = "INSERT INTO carts (user_id, product_id, quantity) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
        jdbcTemplate.update(sql, userId, productId, quantity);
    }

    // 장바구니 상품 출력
    public List<BuyerCartItemResponse> getCartItemsByUserId(Long userId) {
        String sql = "SELECT " +
                "p.product_id AS productId, p.product_img_src AS productImageUrl, p.product_name AS productName, " +
                "p.price AS productPrice, p.quantity AS productQuantity, c.quantity AS cartProductQuantity, s.name AS storeName, s.store_id AS storeId, s.address AS storeAddress, s.payment_method AS storePaymentMethod, s.is_deleted AS storeIsDeleted " +
                "FROM carts c " +
                "JOIN products p ON c.product_id = p.product_id " +
                "JOIN stores s ON p.store_id = s.store_id " +
                "WHERE c.user_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new BuyerCartItemResponse(
                rs.getLong("productId"),
                rs.getString("productImageUrl"),
                rs.getString("productName"),
                rs.getLong("productPrice"),
                rs.getLong("productQuantity"),
                rs.getLong("cartProductQuantity"),
                rs.getString("storeName"),
                rs.getLong("storeId"),
                rs.getString("storeAddress"),
                rs.getInt("storePaymentMethod"),
                rs.getBoolean("storeIsDeleted")
        ), userId);
    }

    // 장바구니 상품 제거
    public void deleteCartItems(Long userId, List<Long> productIds) {
        String sql = "DELETE FROM carts WHERE user_id = :userId AND product_id IN (:productIds)";

        Map<String, Object> params = Map.of(
                "userId", userId,
                "productIds", productIds
        );

        namedParameterJdbcTemplate.update(sql, params);
    }

    // 결제 후 장바구니 상품 삭제
    public void deleteItemsFromCart(Long buyerId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return; // 삭제할 상품이 없으면 아무것도 하지 않음
        }

        String sql = "DELETE FROM carts WHERE user_id = :buyerId AND product_id IN (:productIds)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("buyerId", buyerId);
        params.addValue("productIds", productIds);

        namedParameterJdbcTemplate.update(sql, params);
    }
}

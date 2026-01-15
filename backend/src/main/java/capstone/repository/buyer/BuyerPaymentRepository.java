package capstone.repository.buyer;

import capstone.dto.buyer.request.TossPaymentConfirmRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
public class BuyerPaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public BuyerPaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 현장결제
    public Long onSitePayment(Long buyerId, Long productId, Integer quantity) {
        // 1. 재고 확인 및 감소
        String updateProductsSql = "UPDATE products SET quantity = quantity - ? " +
                "WHERE product_id = ? AND quantity >= ?";

        int affectedRows = jdbcTemplate.update(updateProductsSql, quantity, productId, quantity);
        if (affectedRows == 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 2. 주문서 생성
        String insertOrderSql = "INSERT INTO orderItems (product_id, quantity, seller_id, buyer_id, status, total_price) " +
                "SELECT ?, ?, p.store_id, ?, 1, ? * p.price " +
                "FROM products p " +
                "WHERE p.product_id = ?";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, productId);
            ps.setInt(2, quantity);
            ps.setLong(3, buyerId);
            ps.setInt(4, quantity);
            ps.setLong(5, productId);
            return ps;
        }, keyHolder);

        // 3. products(status) 변경
        String updateStatusSql = "UPDATE products SET status = 1 WHERE product_id = ?";
        jdbcTemplate.update(updateStatusSql, productId);

        // 4. 주문서 order_id 반환
        return keyHolder.getKey().longValue();
    }

    // 상품 ID로 상품 가격 조회
    public Long findPriceByProductId(Long productId) {
        String sql = "SELECT price FROM products WHERE product_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, productId);
    }

    // 카드결제 구현
    public Long createCardPaymentOrder(Long buyerId, TossPaymentConfirmRequest request) {
        // 1. 재고 확인 및 감소
        String updateProductsSql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        int affectedRows = jdbcTemplate.update(updateProductsSql, request.getQuantity(), request.getProductId(), request.getQuantity());
        if (affectedRows == 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 2. 주문서 생성 (DB에 저장)
        String insertOrderSql = "INSERT INTO orderItems (product_id, quantity, total_price, seller_id, buyer_id, status) " +
                "SELECT ?, ?, ?, p.store_id, ?, ? " +
                "FROM products p " +
                "WHERE p.product_id = ?";

        // 예약 여부에 따라 status 결정 (2: 카드결제 수령전, 3: 카드결제 예약중)
        int status = request.isReservation() ? 3 : 2;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, request.getProductId());
            ps.setInt(2, request.getQuantity());
            ps.setLong(3, request.getAmount()); // 검증된 최종 결제 금액 저장
            ps.setLong(4, buyerId);
            ps.setInt(5, status);
            ps.setLong(6, request.getProductId());
            return ps;
        }, keyHolder);

        // 3. products(status) '판매중'으로 변경
        String updateStatusSql = "UPDATE products SET status = 1 WHERE product_id = ?";
        jdbcTemplate.update(updateStatusSql, request.getProductId());

        // 4. 생성된 order_id 반환
        return keyHolder.getKey().longValue();
    }
}

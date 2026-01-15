// src/main/java/capstone/repository/seller/DashboardRepository.java
package capstone.repository.seller;

import capstone.domain.Store;
import capstone.repository.seller.proj.CompletedRow;
import capstone.repository.seller.proj.ReservationRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface SellerDashboardRepository extends Repository<Store, Long> {

  // (1) 실시간 예약 현황 (status = 3)
  @Query(value = """
        SELECT
          oi.order_id AS orderId, p.product_id AS productId, p.product_name AS productName,
          oi.quantity AS quantity, u.name AS userName, qx.valid_until AS validUntil
        FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        JOIN (
            SELECT q.order_id, MAX(q.valid_until) AS valid_until FROM qrCodes q GROUP BY q.order_id
        ) qx ON qx.order_id = oi.order_id
        LEFT JOIN users u ON u.user_id = oi.buyer_id
        WHERE p.store_id = :martId AND oi.status = 3 -- 변경된 부분
        ORDER BY qx.valid_until ASC
    """, countQuery = """
        SELECT COUNT(oi.order_id) FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        WHERE p.store_id = :martId AND oi.status = 3 -- 변경된 부분
    """, nativeQuery = true)
  Page<ReservationRow> findReservations(@Param("martId") Long martId, Pageable pageable);

  // (2) 실시간 구매 현황 (status = 1 or 2)
  @Query(value = """
        SELECT
          oi.order_id AS orderId, p.product_id AS productId, p.product_name AS productName,
          oi.quantity AS quantity, u.name AS userName, qx.valid_until AS validUntil
        FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        JOIN (
            SELECT q.order_id, MAX(q.valid_until) AS valid_until FROM qrCodes q GROUP BY q.order_id
        ) qx ON qx.order_id = oi.order_id
        LEFT JOIN users u ON u.user_id = oi.buyer_id
        WHERE p.store_id = :martId AND oi.status IN (1, 2) -- 변경된 부분
        ORDER BY qx.valid_until ASC
    """, countQuery = """
        SELECT COUNT(oi.order_id) FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        WHERE p.store_id = :martId AND oi.status IN (1, 2) -- 변경된 부분
    """, nativeQuery = true)
  Page<ReservationRow> findPurchases(@Param("martId") Long martId, Pageable pageable);

  // (3) 판매 완료 상품 목록 (status = 4) - 변경 없음
  @Query(value = """
        SELECT
          oi.order_id AS orderId, oi.product_id AS productId, p.product_name AS productName,
          oi.quantity AS quantity, oi.sold_at AS soldAt, u.name as userName
        FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        LEFT JOIN users u ON u.user_id = oi.buyer_id
        WHERE p.store_id = :martId AND oi.status = 4
        ORDER BY oi.sold_at DESC
    """, countQuery = """
        SELECT COUNT(oi.order_id) FROM orderItems oi
        JOIN products p ON p.product_id = oi.product_id
        WHERE p.store_id = :martId AND oi.status = 4
    """, nativeQuery = true)
  Page<CompletedRow> findCompleted(@Param("martId") Long martId, Pageable pageable);
}
package capstone.repository.seller.order;

import capstone.domain.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SellerOrderRepository extends JpaRepository<Sale, Long> {

    // 체크인 대기(1,2,3) 목록
    @Query("""
           select s
             from Sale s
             join fetch s.product p
             join fetch s.buyer   b
            where s.sellerId = :storeId
              and s.status in (1,2,3)
            order by s.orderId desc
           """)
    List<Sale> findReservationsForSeller(@Param("storeId") Long storeId);

    // 판매완료 목록(sold_at 오름차순)
    @Query("""
           select s
             from Sale s
             join fetch s.product p
             join fetch s.buyer   b
            where s.sellerId = :storeId
              and s.status = 4
            order by s.soldAt asc
           """)
    List<Sale> findCompletedForSeller(@Param("storeId") Long storeId);

    // 체크인/취소 처리 시 안전하게 로딩
    @Query("""
           select s
             from Sale s
             join fetch s.product p
            where s.orderId = :orderId
           """)
    Optional<Sale> findByIdWithProduct(@Param("orderId") Long orderId);

    // 상품별 진행/완료 수량 집계 (status는 enum이 아닌 '숫자')
    // inProgressQty: status ∈ (1,2,3) 의 quantity 합
    // completedQty : status = 4 의 quantity 합
    @Query("""
        select new map(
          s.product.productId as productId,
          sum(case when s.status in (1,2,3) then s.quantity else 0 end) as inProgressQty,
          sum(case when s.status = 4 then s.quantity else 0 end)       as completedQty
        )
        from Sale s
        where s.sellerId = :storeId
        group by s.product.productId
    """)
    List<Map<String, Object>> sumByProductForStore(@Param("storeId") Long storeId);

    // 특정 상품에 대한 상태(1,2,3) 건수 조회
    @Query("""
            SELECT count(s)
              FROM Sale s
             WHERE s.product.productId = :productId
               AND s.status IN (1, 2, 3)
            """)
    long countActiveSalesByProductId(@Param("productId") Long productId);
}

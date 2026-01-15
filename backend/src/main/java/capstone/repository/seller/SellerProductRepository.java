package capstone.repository.seller;
//8월 11일 추가

import capstone.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SellerProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStoreId(Long storeId);

    List<Product> findByStoreIdAndStatus(Long storeId, Integer status);

    // 추가: 재고 조정을 위해 상품 단건 조회 (스토어 소속 확인)
    Optional<Product> findByProductIdAndStoreId(Long productId, Long storeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Product p 
              SET p.status = :status
            WHERE p.storeId = :storeId
              AND p.productId IN (:ids)
           """)
    int bulkUpdateStatus(@Param("storeId") Long storeId,
                         @Param("ids") List<Long> ids,
                         @Param("status") Integer status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Product p
              SET p.status = 2
            WHERE (p.quantity <= 0 OR p.expirationDate < :now)
              AND p.status <> 2
           """)
    int markSoldOutWhenZeroOrExpired(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE Product p
              SET p.status = 1
            WHERE p.quantity > 0
              AND p.expirationDate >= :now
              AND p.status <> 0
              AND p.status <> 1
           """)
    int markOnSaleWhenAvailable(@Param("now") LocalDateTime now);
}

package capstone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orderItems")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    // products.product_id FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "sold_at")
    private LocalDateTime soldAt;

    // stores.store_id FK (숫자 컬럼 유지)
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // users.user_id FK (숫자 컬럼)
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    // 선택: 구매자 엔티티 지연 로딩
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    // 주문 상태 (0~4: 의미는 DB CHECK 제약조건 따라감)
    // 0: 판매취소, 1: 현장결제 수령전, 2: 카드결제 수령전, 3: 카드결제 예약중, 4: 판매완료
    @Column(name = "status", nullable = false)
    private Integer status;
}


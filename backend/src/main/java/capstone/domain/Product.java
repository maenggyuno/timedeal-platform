package capstone.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "product_img_src")
    private String productImgSrc;

//    @Column(name = "product_name", nullable = false)
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

//    @Column(nullable = false)
    @Column(name = "category", nullable = false, length = 50)
    private String category;

//    @Column(nullable = false)
    @Column(name = "price", nullable = false)
    private Long price;

//    @Column(nullable = false)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "registration_date", nullable = false, updatable = false)
    private LocalDateTime registrationDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

//    @Column(nullable = false)
    @Column(name = "status", nullable = false)
    private Integer status;  // 0: 판매대기, 1: 판매중, 2: 판매완료

//    @Column(columnDefinition = "TEXT")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}

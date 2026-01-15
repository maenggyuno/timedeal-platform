//package capstone.dto.seller.request;
//
////7월 28일 균오 생성
//
//import capstone.domain.Category;  // 상품 분류 enum
//
//public record ProductSaveRequest(
//        String productName,
//        Long price,
//        Integer quantity,
//        Category category,
//        String expirationDateTime, // ISO‑8601
//        String description,
//        String imgUrl
//){}


// src/main/java/capstone/dto/seller/request/ProductSaveRequest.java
//package capstone.dto.seller.request;
//
//import lombok.*;
//
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class ProductSaveRequest {
//    private String productImgSrc;       // base64 또는 URL
//    private String productName;
//    private String category;
//    private Long price;
//    private Integer quantity;
//    /**
//     * ISO 8601 문자열로 넘길 것
//     * 예시: "2025-08-06T14:30:00"
//     */
//    private String expirationDate;
//    private String description;
//}

// src/main/java/capstone/dto/seller/request/ProductSaveRequest.java

//package capstone.dto.seller.request;
//
//import lombok.*;
//
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//public class ProductSaveRequest {
//    private String category;
//    private String productName;
//    private Long   price;
//    private Integer quantity;
//    /**
//     * ISO 8601 문자열 (예: "2025-08-06T14:30:00")
//     */
//    private String expirationDate;
//    private String description;
//}

package capstone.dto.seller.request;
//8월 9일 균오 수정
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SellerProductSaveRequest {
    private String  category;
    private String  productName;
    private Long    price;
    private Integer quantity;
    private String  expirationDate;
    private String  description;

    // 추가: S3에 올린 뒤 프론트에서 채워서 보내줄 이미지 경로/URL
    private String  productImgSrc;  // 예: "products/uuid_name.png" 또는 공개 URL
}

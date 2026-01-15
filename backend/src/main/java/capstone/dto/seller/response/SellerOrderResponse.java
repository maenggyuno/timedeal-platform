//9월 24일 균오 수정
package capstone.dto.seller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerOrderResponse {
    private Long orderId;
    private String buyerName;
    private String productName;
    private Integer quantity;
    private Long price;
    private Long totalPrice;
    private Integer status;
    private Long sellerId;
    private String soldAt;       // ISO 문자열(또는 "yyyy-MM-dd HH:mm")
    private String qrValidUntil; // 체크인 탭에서 카운트다운 용
}


package capstone.dto.buyer.request;

import lombok.Getter;

import java.util.List;

@Getter
public class BuyerCartPaymentRequest {

    // 장바구니에 담긴 상품 목록
    private List<BuyerCartItemRequest> items;

    // 카드 결제 시 필요한 통합 결제 정보
    private String paymentKey;
    private String orderId;
    private Long amount;

    public BuyerCartPaymentRequest(String paymentKey, String orderId, Long amount) {
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.amount = amount;
    }
}

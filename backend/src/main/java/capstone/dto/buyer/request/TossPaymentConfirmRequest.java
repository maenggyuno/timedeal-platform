package capstone.dto.buyer.request;

import lombok.Getter;

@Getter
public class TossPaymentConfirmRequest {

    // 토스페이먼츠 API 연동에 필요한 정보
    private String paymentKey;
    private String orderId;

    // 결제 및 상품 관련 정보
    private Long amount;
    private Long productId;
    private Integer quantity;
    private boolean isReservation;

    // 서비스 레이어에서 DB 저장을 위해 객체를 생성할 때 사용할 생성자
    public TossPaymentConfirmRequest(Long productId, Integer quantity, Long amount, boolean isReservation) {
        this.productId = productId;
        this.quantity = quantity;
        this.amount = amount;
        this.isReservation = isReservation;
    }
}

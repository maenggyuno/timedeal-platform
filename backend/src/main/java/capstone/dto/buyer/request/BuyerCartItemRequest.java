package capstone.dto.buyer.request;

import lombok.Getter;

@Getter
public class BuyerCartItemRequest {
    private Long productId;
    private Integer quantity;
    private String paymentMethod;
    private boolean isReservation;
}

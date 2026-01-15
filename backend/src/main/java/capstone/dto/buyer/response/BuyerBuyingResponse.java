package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BuyerBuyingResponse {

    private Long orderId;
    private Long productId;
    private String imageUrl;
    private String productName;
    private Integer quantity;
    private Long totalPrice;
    private LocalDateTime validUntil;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private String creditMethod;
}

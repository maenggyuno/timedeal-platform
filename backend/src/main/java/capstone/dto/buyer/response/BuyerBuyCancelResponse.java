package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuyerBuyCancelResponse {

    private Long orderId;
    private Long productId;
    private String imageUrl;
    private String productName;
    private Integer quantity;
    private Long totalPrice;
    private Long storeId;
    private String storeName;
    private String storeAddress;
    private Boolean storeIsDeleted;
}

package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BuyerCartItemResponse {

    private Long productId;
    private String productImageUrl;
    private String productName;
    private Long productPrice;
    private Long productQuantity;
    private Long cartProductQuantity;
    private String storeName;
    private Long storeId;
    private String storeAddress;
    private Integer storePaymentMethod;
    private Boolean storeIsDeleted;

}

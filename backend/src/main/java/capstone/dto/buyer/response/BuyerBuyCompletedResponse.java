package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BuyerBuyCompletedResponse {

    private Long orderId;
    private Long productId;
    private String productImgSrc;
    private String productName;
    private Integer quantity;
    private Long totalPrice;
    private Long storeId;
    private Boolean storeIsDeleted;
    private String storeName;
    private String storeAddress;
    private Boolean isReviewed;
    private LocalDateTime reviewCreatedAt;
}

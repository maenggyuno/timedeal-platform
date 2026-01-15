package capstone.dto.buyer.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BuyerOrderCompleteResponse {

    private Long orderId;
    private String productImgSrc;
    private String productName;
    private Integer quantity;
    private Long price;
    private Long totalPrice;
    private LocalDateTime validUntil;
    private Integer status;
}

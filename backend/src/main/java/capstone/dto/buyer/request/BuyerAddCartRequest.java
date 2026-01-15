package capstone.dto.buyer.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerAddCartRequest {

    private Long productId;
    private Long quantity;
}

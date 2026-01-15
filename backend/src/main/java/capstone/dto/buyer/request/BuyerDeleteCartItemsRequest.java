package capstone.dto.buyer.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuyerDeleteCartItemsRequest {

    private List<Long> productIds;
}

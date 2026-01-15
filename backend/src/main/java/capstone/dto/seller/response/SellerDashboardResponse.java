package capstone.dto.seller.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SellerDashboardResponse {

    private Long storeId;
    private String name;
    private String address;
    private Integer authority;
}

package capstone.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SellerMyMartInfoResponse {

    private Long storeId;
    private String storeName;
    private String storeAddress;
    private String phoneNumber;
    private String openTime;
    private String closeTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
}

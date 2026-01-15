package capstone.dto.qrcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrCodeProductResponse {

    private Long orderId;
    private String productName;
    private Integer quantity;
    private Long totalPrice;
    private Integer status;
}

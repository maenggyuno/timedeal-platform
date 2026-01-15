package capstone.dto.buyer.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class BuyerQrcodeExtensionResponse {

    private Integer count;
    private LocalDateTime validUntil;
    private LocalTime openingTime;
    private LocalTime closingTime;
}

package capstone.dto.qrcode;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QrCodeRequest {

    private String uuid;
    private LocalDateTime validUntil;
    private Long orderId;
}

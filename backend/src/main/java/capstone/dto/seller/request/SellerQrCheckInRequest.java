//9월 24일 신규 추가
package capstone.dto.seller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerQrCheckInRequest {

    @NotNull(message = "orderId는 필수입니다.")
    private Long orderId;

    @NotBlank(message = "uuid는 필수입니다.")
    private String uuid;
}

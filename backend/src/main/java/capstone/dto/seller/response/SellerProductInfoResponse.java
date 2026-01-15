package capstone.dto.seller.response;
//8월 9일 균오 변경 등록확인 페이지 이미지 확인용
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProductInfoResponse {
    private Long productId;
    private String productName;
    private Long price;
    private Integer quantity;
    private String category;
    private String productImgSrc;
    private String registrationDate;
    private String expirationDate;
    private String description;
    private Integer status;
    private Long storeId;
}


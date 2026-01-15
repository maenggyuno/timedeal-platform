package capstone.dto.seller.request;

//8월 11일 재고 수동 수정 신규 추가, 8월 19일
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class SellerAdjustStockRequest {
    private int delta;   // +N / -N
}

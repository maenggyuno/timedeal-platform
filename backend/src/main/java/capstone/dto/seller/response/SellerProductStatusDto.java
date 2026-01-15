//10월 3일 수정 등록 상품 확인 페이지에서 판매대기 완료 판매중 모두에 표시되거나 아무튼 그 파일임
package capstone.dto.seller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SellerProductStatusDto {

    private Long productId;
    private String productName;
    private String productImgSrc;

    // 3가지 상태의 수량을 모두 담을 필드
    private int waitingCount;   // 판매대기 수량
    private int inSaleCount;    // 판매중(예약) 수량
    private int completedCount; // 판매완료 수량
}
package capstone.dto.seller.request;

// 8월 9일 등록완료 후에 상태변경 만들때 신규로 넣은 파일
// 250809 변경: 신규 파일

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellerBulkStatusUpdateRequest {
    private List<Long> productIds; // 변경 대상
    private Integer status;        // 0:판매대기, 1:판매중, 2:판매완료
}

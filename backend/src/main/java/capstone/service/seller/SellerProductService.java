package capstone.service.seller;

// 8월 11일 균오 수정 수동 재고 수량 추가
// 10월 3일 수정

import capstone.domain.Product;
import capstone.dto.seller.request.SellerProductSaveRequest;
import capstone.dto.seller.response.SellerProductInfoResponse;
import capstone.dto.seller.response.SellerProductStatusDto; // DTO 임포트

import java.util.List;

public interface SellerProductService {
    // JSON만 받는다 (프리사인드 업로드이므로 파일X)
    Product adjustStock(Long storeId, Long productId, int delta);
    void save(Long storeId, SellerProductSaveRequest req);

    // 250809 변경: 상태 필터 지원
    List<SellerProductInfoResponse> list(Long storeId, Integer status);

    // 250809 변경: 일괄 상태변경
    int bulkUpdateStatus(Long storeId, List<Long> productIds, Integer status);

    // 상품 종합 현황 조회를 위한 메소드 선언
    List<SellerProductStatusDto> getProductStatusesForStore(Long storeId);
}


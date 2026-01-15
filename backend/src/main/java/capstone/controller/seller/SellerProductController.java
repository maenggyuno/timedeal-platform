package capstone.controller.seller;

//8월 9일 균오 수정
//10월 3일 수정
import capstone.domain.Product;
import capstone.dto.seller.request.SellerAdjustStockRequest;
import capstone.dto.seller.request.SellerBulkStatusUpdateRequest;
import capstone.dto.seller.request.SellerProductSaveRequest;
import capstone.dto.seller.response.SellerProductInfoResponse;
import capstone.service.seller.SellerProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import capstone.dto.seller.response.SellerProductStatusDto; // DTO 임포트

import java.util.List;

@RestController
@RequestMapping("/api/seller/stores/{storeId}/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService service;

    // 변경: JSON만 받는다 (프리사인드로 이미지 업로드)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> registerProduct(
            @PathVariable Long storeId,
            @RequestBody SellerProductSaveRequest req
    ) {
        service.save(storeId, req);
        return ResponseEntity.ok().build();
    }

    // 250809 변경: 상태 필터 지원 (status 파라미터는 0/1/2, 없으면 전체)
    @GetMapping
    public ResponseEntity<List<SellerProductInfoResponse>> list(
            @PathVariable Long storeId,
            @RequestParam(required = false) Integer status
    ) {
        return ResponseEntity.ok(service.list(storeId, status));
    }

    // 250809 변경: 일괄 상태변경 엔드포인트
    @PatchMapping(path = "/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> bulkUpdateStatus(
            @PathVariable Long storeId,
            @RequestBody SellerBulkStatusUpdateRequest req
    ) {
        int updated = service.bulkUpdateStatus(storeId, req.getProductIds(), req.getStatus());
        return ResponseEntity.ok().body(new java.util.HashMap<>() {{
            put("updated", updated);
        }});
    }

    // 추가: 수동 재고 증감
    @PatchMapping(path = "/{productId}/stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> adjustStock(
            @PathVariable Long storeId,
            @PathVariable Long productId,
            @RequestBody SellerAdjustStockRequest req
    ) {
        Product updated = service.adjustStock(storeId, productId, req.getDelta());
        return ResponseEntity.ok(updated);
    }

    // [신규 추가] 상품 종합 현황 조회 API
    @GetMapping("/status-summary")
    public ResponseEntity<List<SellerProductStatusDto>> getStatusSummary(@PathVariable Long storeId) {
        List<SellerProductStatusDto> summaryList = service.getProductStatusesForStore(storeId);
        return ResponseEntity.ok(summaryList);
    }
}







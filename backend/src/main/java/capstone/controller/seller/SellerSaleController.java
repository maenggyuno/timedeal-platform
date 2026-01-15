package capstone.controller.seller;

import capstone.dto.seller.response.SellerSaleHistoryResponse;
import capstone.service.seller.SellerSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/stores/{storeId}/sales")
@RequiredArgsConstructor
public class SellerSaleController {
    private final SellerSaleService saleService;

    // 수정됨: status 필터 없이도 오류 안 나도록 오버로드 형태로 간소화
    @GetMapping
    public ResponseEntity<List<SellerSaleHistoryResponse>> list(
            @PathVariable Long storeId
    ) {
        return ResponseEntity.ok(saleService.list(storeId, null));
    }
}



//9월 24일 균오 수정
package capstone.controller.seller.order;

import capstone.dto.seller.request.SellerQrCheckInRequest;
import capstone.dto.seller.response.SellerOrderResponse;
import capstone.service.seller.order.impl.SellerSaleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerOrderController {

    private final SellerSaleServiceImpl service;

    // /api/seller/{storeId}/orders?tab=checkin|completed
    @GetMapping("/{storeId}/orders")
    public ResponseEntity<List<SellerOrderResponse>> list(
            @PathVariable Long storeId,
            @RequestParam(defaultValue = "checkin") String tab) {

        if ("completed".equalsIgnoreCase(tab)) {
            return ResponseEntity.ok(service.getCompletedForSeller(storeId));
        }
        return ResponseEntity.ok(service.getReservationsForSeller(storeId));
    }

    // 체크인(판매완료) - QR uuid 필요
    @PatchMapping("/orders/{orderId}/checkin")
    public ResponseEntity<Void> checkIn(
            @PathVariable Long orderId,
            @RequestBody SellerQrCheckInRequest req) {
        service.checkInWithQr(orderId, req.getUuid());
        return ResponseEntity.noContent().build();
    }

    // 판매 취소
    @PatchMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long orderId) {
        service.cancel(orderId);
        return ResponseEntity.noContent().build();
    }
}

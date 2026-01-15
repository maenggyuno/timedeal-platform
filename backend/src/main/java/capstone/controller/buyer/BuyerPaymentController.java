package capstone.controller.buyer;

import capstone.dto.buyer.request.BuyerCartPaymentRequest;
import capstone.dto.buyer.request.TossPaymentConfirmRequest;
import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buyer/pay")
public class BuyerPaymentController {

    private final BuyerPaymentService buyerPaymentService;

    public BuyerPaymentController(BuyerPaymentService buyerPaymentService) {
        this.buyerPaymentService = buyerPaymentService;
    }

    /**
     *  현장결제
     * */
    @PostMapping("/on-site")
    public ResponseEntity<Map<String, Long>> onSitePayment(@AuthenticationPrincipal UserPrincipal user,
                                                           @RequestParam Long productId,
                                                           @RequestParam Integer quantity) {
        Long orderId = buyerPaymentService.onSitePayment(user.getUserId(), productId, quantity);

        Map<String, Long> response = new HashMap<>();
        response.put("orderId", orderId);

        return ResponseEntity.ok(response);
    }

    /**
     *  카드결제
     * */
    @PostMapping("/toss/confirm")
    public ResponseEntity<Map<String, Long>> confirmTossPayment(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody TossPaymentConfirmRequest request) {
        
        Long finalOrderId = buyerPaymentService.confirmTossPayment(user.getUserId(), request);

        Map<String, Long> response = new HashMap<>();
        response.put("orderId", finalOrderId);

        return ResponseEntity.ok(response);
    }

    /**
     *  장바구니 결제 처리
     * */
    @PostMapping("/cart/process")
    public ResponseEntity<Map<String, Object>> processCartPayment(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody BuyerCartPaymentRequest request) {

        List<Long> orderIds = buyerPaymentService.processCartPayment(user.getUserId(), request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "결제가 성공적으로 처리되었습니다.");
        response.put("orderIds", orderIds);

        return ResponseEntity.ok(response);
    }
}

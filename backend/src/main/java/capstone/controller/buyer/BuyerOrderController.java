package capstone.controller.buyer;

import capstone.dto.buyer.response.BuyerBuyCancelResponse;
import capstone.dto.buyer.response.BuyerBuyCompletedResponse;
import capstone.dto.buyer.response.BuyerBuyingResponse;
import capstone.dto.buyer.response.BuyerOrderCompleteResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/order")
public class BuyerOrderController {

    private final BuyerOrderService buyerOrderService;

    public BuyerOrderController(BuyerOrderService buyerOrderService) {
        this.buyerOrderService = buyerOrderService;
    }

    /**
     *  단일상품 주문완료시 출력
     * */
    @GetMapping("/single-complete")
    public ResponseEntity<BuyerOrderCompleteResponse> getOrderComplete(@RequestParam Long orderId) {
        BuyerOrderCompleteResponse response = buyerOrderService.getOrderComplete(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     *  여러상품 주문완료시 출력
     *  - ex. 장바구니 결제
     * */
    @GetMapping("/cart-complete")
    public ResponseEntity<List<BuyerOrderCompleteResponse>> getCartOrderComplete(@RequestParam List<Long> orderIds) {
        List<BuyerOrderCompleteResponse> response = buyerOrderService.getCartOrderComplete(orderIds);
        return ResponseEntity.ok(response);
    }

    /**
     *  구매 중인 상품 출력
     * */
    @GetMapping("/buy-ing")
    public ResponseEntity<List<BuyerBuyingResponse>> getBuying(@AuthenticationPrincipal UserPrincipal user) {
        List<BuyerBuyingResponse> buying = buyerOrderService.getBuying(user.getUserId());
        return ResponseEntity.ok(buying);
    }

    /**
     *  구매 가능시간 연장
     * */
    @PostMapping("/valid-until-extension")
    public ResponseEntity<String> validUntilExtension(@RequestParam Long orderId) {
        String response = buyerOrderService.validUntilExtension(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     *  구매중인 상품 구매 취소
     * */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelOrder(@RequestParam Long orderId) {
        buyerOrderService.cancelOrder(orderId);
        return ResponseEntity.ok("주문이 성공적으로 취소되었습니다.");
    }

    /**
     *  구매 취소된 상품 출력
     * */
    @GetMapping("/buy-cancel")
    public ResponseEntity<List<BuyerBuyCancelResponse>> getBuyCancel(@AuthenticationPrincipal UserPrincipal user) {
        List<BuyerBuyCancelResponse> buyCancel = buyerOrderService.getBuyCancel(user.getUserId());
        return ResponseEntity.ok(buyCancel);
    }

    /**
     *  구매 완료된 상품 출력
     * */
    @GetMapping("/buy-completed")
    public ResponseEntity<List<BuyerBuyCompletedResponse>> getCompletedOrders(@AuthenticationPrincipal UserPrincipal user) {
        List<BuyerBuyCompletedResponse> completed = buyerOrderService.getCompletedOrders(user.getUserId());
        return ResponseEntity.ok(completed);
    }
}

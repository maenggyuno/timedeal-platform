package capstone.controller.buyer;

import capstone.dto.buyer.request.ReviewCreateRequest;
import capstone.dto.buyer.response.ReviewResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyer/review")
public class BuyerReviewController {

    private final BuyerReviewService buyerReviewService;

    public BuyerReviewController(BuyerReviewService buyerReviewService) {
        this.buyerReviewService = buyerReviewService;
    }

    /**
     *  리뷰 생성 API
     * */
    @PostMapping("/{orderId}")
    public ResponseEntity<Void> createReview(@PathVariable Long orderId,
                                             @AuthenticationPrincipal UserPrincipal user,
                                             @RequestBody ReviewCreateRequest request) {
        buyerReviewService.createReview(orderId, user.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     *  주문 ID로 리뷰 조회 API
     * */
    @GetMapping("/{orderId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long orderId) {
        return buyerReviewService.getReviewByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

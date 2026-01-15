package capstone.controller.seller;

import capstone.dto.seller.SellerReviewDto;
import capstone.dto.seller.SellerReviewSummaryDto;
import capstone.service.seller.SellerReviewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/store")
public class SellerReviewController {

    private final SellerReviewService service;

    public SellerReviewController(SellerReviewService service) {
        this.service = service;
    }

    @GetMapping("/{storeId}/reviews")
    public List<SellerReviewDto> getReviews(@PathVariable long storeId) {
        return service.getReviewsForStore(storeId);
    }

    @GetMapping("/{storeId}/review-summary")
    public SellerReviewSummaryDto getSummary(@PathVariable long storeId) {
        return service.getReviewSummary(storeId);
}
}

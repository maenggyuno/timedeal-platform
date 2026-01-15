package capstone.service.buyer;

import capstone.dto.buyer.request.ReviewCreateRequest;
import capstone.dto.buyer.response.ReviewResponse;
import capstone.repository.buyer.BuyerReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class BuyerReviewService {

    private final BuyerReviewRepository buyerReviewRepository;

    public BuyerReviewService(BuyerReviewRepository buyerReviewRepository) {
        this.buyerReviewRepository = buyerReviewRepository;
    }

    @Transactional
    public void createReview(Long orderId, Long userId, ReviewCreateRequest request) {
        buyerReviewRepository.save(orderId, userId, request.getRating(), request.getContent());
    }

    public Optional<ReviewResponse> getReviewByOrderId(Long orderId) {
        return buyerReviewRepository.findByOrderId(orderId);
    }
}

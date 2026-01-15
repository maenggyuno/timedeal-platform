package capstone.service.seller;

import capstone.dto.seller.SellerReviewDto;
import capstone.dto.seller.SellerReviewSummaryDto;
import capstone.repository.seller.SellerReviewQueryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerReviewService {

    private final SellerReviewQueryRepository repo;

    public SellerReviewService(SellerReviewQueryRepository repo) {
        this.repo = repo;
    }

    public List<SellerReviewDto> getReviewsForStore(long storeId) {
        return repo.findReviewsByStoreId(storeId);
    }

    public SellerReviewSummaryDto getReviewSummary(long storeId) {
        return repo.getSummary(storeId);
    }
}

package capstone.service.seller.impl;

// 250809 변경: 신규 파일
// 8월 11일 시작 신규파일

import capstone.domain.Product;
import capstone.repository.seller.SellerProductRepository;
import capstone.service.seller.SellerProductAutoStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SellerProductAutoStatusServiceImpl implements SellerProductAutoStatusService {

    private final SellerProductRepository repo;

    @Override
    @Transactional
    public void syncByProductId(Long productId) {
        Product p = repo.findById(productId).orElse(null);
        if (p == null) return;

        // 운영자 숨김(0)은 자동으로 건들지 않음
        if (p.getStatus() != null && p.getStatus() == 0) return;

        boolean expired = p.getExpirationDate() != null && p.getExpirationDate().isBefore(LocalDateTime.now());
        boolean noStock = p.getQuantity() != null && p.getQuantity() <= 0;

        if (expired || noStock) {
            if (p.getStatus() == null || p.getStatus() != 2) {
                p.setStatus(2); // 판매완료
            }
        } else {
            if (p.getStatus() == null || p.getStatus() != 1) {
                p.setStatus(1); // 판매중
            }
        }
    }

    @Override
    @Transactional
    public int runBulkSync() {
        LocalDateTime now = LocalDateTime.now();
        int a = repo.markSoldOutWhenZeroOrExpired(now);
        int b = repo.markOnSaleWhenAvailable(now);
        return a + b;
    }
}

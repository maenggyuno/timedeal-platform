//10월 3일 수정
package capstone.service.seller.impl;

import capstone.domain.Product;
import capstone.dto.seller.request.SellerProductSaveRequest;
import capstone.dto.seller.response.SellerProductInfoResponse;
import capstone.dto.seller.response.SellerProductStatusDto; // 새로 만든 DTO 임포트
import capstone.repository.seller.SellerProductRepository;
import capstone.service.seller.SellerProductService;
import capstone.service.seller.SellerSaleService; // ✨ SellerSaleService1 주입
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList; // ✨ 임포트
import java.util.List;
import java.util.Map; // ✨ 임포트

@Service
@RequiredArgsConstructor
@Transactional
public class SellerProductServiceImpl implements SellerProductService {

    private final SellerProductRepository repo;
    private final SellerSaleService saleService; // 집계 메소드를 사용하기 위해 주입

    @Override
    public void save(Long storeId, SellerProductSaveRequest req) {
        final String imgUrl = req.getProductImgSrc() == null ? "" : req.getProductImgSrc();
        final LocalDateTime expiration = LocalDateTime.parse(req.getExpirationDate());

        Product p = Product.builder()
                .storeId(storeId)
                .productImgSrc(imgUrl)
                .productName(req.getProductName())
                .category(req.getCategory())
                .price(req.getPrice())
                .quantity(req.getQuantity())
                .expirationDate(expiration)
                .description(req.getDescription())
                .status(0)
                .build();

        repo.save(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerProductInfoResponse> list(Long storeId, Integer status) {
        List<Product> products = (status == null)
                ? repo.findByStoreId(storeId)
                : repo.findByStoreIdAndStatus(storeId, status);

        return products.stream()
                .map(p -> SellerProductInfoResponse.builder()
                        .productId(p.getProductId())
                        .productName(p.getProductName())
                        .price(p.getPrice())
                        .quantity(p.getQuantity())
                        .category(p.getCategory())
                        .productImgSrc(p.getProductImgSrc())
                        .registrationDate(p.getRegistrationDate() == null ? null : p.getRegistrationDate().toString())
                        .expirationDate(p.getExpirationDate() == null ? null : p.getExpirationDate().toString())
                        .description(p.getDescription())
                        .status(p.getStatus())
                        .storeId(p.getStoreId())
                        .build()
                )
                .toList();
    }

    @Override
    public int bulkUpdateStatus(Long storeId, List<Long> productIds, Integer status) {
        if (productIds == null || productIds.isEmpty()) return 0;
        if (status == null || status < 0 || status > 2) {
            throw new IllegalArgumentException("invalid status (must be 0,1,2)");
        }
        return repo.bulkUpdateStatus(storeId, productIds, status);
    }

    // 추가: 수동 재고 증감
    @Override
    public Product adjustStock(Long storeId, Long productId, int delta) {
        Product p = repo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!p.getStoreId().equals(storeId)) {
            throw new org.springframework.security.access.AccessDeniedException("store mismatch");
        }

        int newQty = Math.max(0, p.getQuantity() + delta);
        p.setQuantity(newQty);

        // 상태 자동 조정
        var now = LocalDateTime.now();
        if (newQty <= 0 || p.getExpirationDate().isBefore(now)) {
            p.setStatus(2); // 판매완료
        } else {
            if (p.getStatus() != 0) p.setStatus(1); // 운영자가 0(대기)로 잠근건 유지
        }
        return repo.save(p);
    }

    // [신규 구현] 인터페이스에 추가한 메소드의 실제 구현부
    @Override
    @Transactional(readOnly = true)
    public List<SellerProductStatusDto> getProductStatusesForStore(Long storeId) {
        // 1. 해당 상점의 모든 상품 목록을 가져옴
        List<Product> products = repo.findByStoreId(storeId);

        // 2. saleService의 집계 메소드를 호출해 '판매중'/'판매완료' 수량을 한번에 가져옴
        Map<Long, long[]> saleCounts = saleService.sumByProductForStore(storeId);

        // 3. 상품 목록을 순회하며 3가지 상태의 재고를 모두 계산하고 DTO 리스트를 생성함
        List<SellerProductStatusDto> result = new ArrayList<>();
        for (Product product : products) {
            long productId = product.getProductId();
            int totalStock = product.getQuantity() != null ? product.getQuantity() : 0;

            long[] counts = saleCounts.getOrDefault(productId, new long[]{0, 0});
            long inSaleCount = counts[0];
            long completedCount = counts[1];

            // '판매대기' 수량 계산
            long waitingCount = totalStock;

            result.add(SellerProductStatusDto.builder()
                    .productId(productId)
                    .productName(product.getProductName())
                    .productImgSrc(product.getProductImgSrc())
                    .waitingCount((int)Math.max(0, waitingCount)) // 음수가 되지 않도록
                    .inSaleCount((int)inSaleCount)
                    .completedCount((int)completedCount)
                    .build());
        }
        return result;
    }
}
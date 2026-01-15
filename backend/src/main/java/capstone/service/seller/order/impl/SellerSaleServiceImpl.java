package capstone.service.seller.order.impl;

import capstone.domain.Product;
import capstone.domain.Sale;
import capstone.dto.seller.response.SellerOrderResponse;
import capstone.dto.seller.response.SellerSaleHistoryResponse;
import capstone.repository.QrCodeRepository;
import capstone.repository.seller.order.SellerOrderRepository;
import capstone.service.seller.SellerSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
// 최종 인터페이스인 SellerSaleService 를 구현(implements)하도록 변경
public class SellerSaleServiceImpl implements SellerSaleService {

    private final SellerOrderRepository orderRepo;
    private final QrCodeRepository qrRepo;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(readOnly = true)
    public Map<Long, long[]> sumByProductForStore(Long storeId) {
        List<Map<String, Object>> results = orderRepo.sumByProductForStore(storeId);
        Map<Long, long[]> summary = new HashMap<>();
        for (Map<String, Object> row : results) {
            Long productId = (Long) row.get("productId");
            long inProgressQty = ((Number) row.getOrDefault("inProgressQty", 0)).longValue();
            long completedQty = ((Number) row.getOrDefault("completedQty", 0)).longValue();
            summary.put(productId, new long[]{inProgressQty, completedQty});
        }
        return summary;
    }

    @Override
    @Transactional
    public void checkInWithQr(Long orderId, String uuid) {
        var sale = orderRepo.findByIdWithProduct(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        var qr = qrRepo.findByOrderIdAndUuid(orderId, uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR이 유효하지 않습니다."));
        if (qr.getValidUntil().isBefore(LocalDateTime.now(ZoneId.of("UTC")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR이 만료되었습니다.");
        }

        if (sale.getStatus() != null && sale.getStatus() == 4) return;

        sale.setStatus(4); // '판매완료'
        sale.setSoldAt(LocalDateTime.now(KST));

        qrRepo.deleteByOrderId(orderId);
    }

    /**
     * 주문 취소 로직 (요청사항 반영)
     */
    @Override
    @Transactional
    public void cancel(Long orderId) {
        // 주문(Sale)과 연관된 상품(Product) 정보를 함께 조회
        Sale sale = orderRepo.findByIdWithProduct(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        // 이미 취소된 주문이면 아무 작업도 하지 않음
        if (sale.getStatus() != null && sale.getStatus() == 0) {
            return;
        }

        Product product = sale.getProduct();
        int cancelledQuantity = sale.getQuantity();

        // 상품의 재고(quantity)를 취소된 주문의 수량만큼 다시 늘려줌
        product.setQuantity(product.getQuantity() + cancelledQuantity);

        // 주문(Sale)의 상태를 취소(0)으로 변경
        sale.setStatus(0);

        // 해당 주문과 연결된 QR 코드를 삭제
        qrRepo.deleteByOrderId(orderId);

        // 이 상품에 대해 다른 상태 (1,2,3)이 더 있는지 확인
        long activeSaleCount = orderRepo.countActiveSalesByProductId(product.getProductId());

        // 존재하지 않는다면, 취소 후에는 상품 상태가 0이 되도록 변경
        if (activeSaleCount == 1) {
            product.setStatus(0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderResponse> getReservationsForSeller(Long storeId) {
        return orderRepo.findReservationsForSeller(storeId).stream()
                .map(s -> {
                    var price = s.getProduct().getPrice();
                    var qty   = s.getQuantity();
                    var qrValidUntil = qrRepo.findLatestByOrderId(s.getOrderId())
                            .map(q -> q.getValidUntil().atZone(KST).format(FMT))
                            .orElse(null);
                    return SellerOrderResponse.builder()
                            .orderId(s.getOrderId()).buyerName(s.getBuyer().getName())
                            .productName(s.getProduct().getProductName()).quantity(qty).price(price)
                            .totalPrice(price != null ? price * qty : 0L).status(s.getStatus())
                            .sellerId(s.getSellerId()).soldAt(null).qrValidUntil(qrValidUntil).build();
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SellerOrderResponse> getCompletedForSeller(Long storeId) {
        return orderRepo.findCompletedForSeller(storeId).stream()
                .map(s -> {
                    var price = s.getProduct().getPrice();
                    var qty   = s.getQuantity();
                    var soldAtStr = s.getSoldAt() == null ? null : s.getSoldAt().atZone(KST).format(FMT);
                    return SellerOrderResponse.builder()
                            .orderId(s.getOrderId()).buyerName(s.getBuyer().getName())
                            .productName(s.getProduct().getProductName()).quantity(qty).price(price)
                            .totalPrice(price != null ? price * qty : 0L).status(s.getStatus())
                            .sellerId(s.getSellerId()).soldAt(soldAtStr).qrValidUntil(null).build();
                }).toList();
    }

    @Override
    public List<SellerSaleHistoryResponse> list(Long storeId, Object o) {
        // TODO: 필요 시 판매 내역 조회 로직 구현
        return null;
    }
}
package capstone.service.buyer;

import capstone.dto.buyer.response.*;
import capstone.repository.buyer.BuyerOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
public class BuyerOrderService {

    private final BuyerOrderRepository buyerOrderRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public BuyerOrderService(BuyerOrderRepository buyerOrderRepository) {
        this.buyerOrderRepository = buyerOrderRepository;
    }

    /**
     *  단일상품 주문완료시 출력
     * */
    public BuyerOrderCompleteResponse getOrderComplete(Long orderId) {
        List<BuyerOrderCompleteResponse> results = buyerOrderRepository.getOrderComplete(orderId);

        if (results.isEmpty()) {
            return null;
        }

        return results.get(0);
    }

    /**
     *  여러상품 주문완료시 출력 (ex. 장바구니 결제)
     * */
    public List<BuyerOrderCompleteResponse> getCartOrderComplete(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return buyerOrderRepository.findOrdersByIds(orderIds);
    }

    /**
     *  구매 중인 상품 출력
     * */
    public List<BuyerBuyingResponse> getBuying(Long userId) {
        return buyerOrderRepository.getBuying(userId);
    }

    /**
     *  구매 가능시간 연장
     * */
    public String validUntilExtension(Long orderId) {
        // QR코드 정보 및 가게정보 조회
        BuyerQrcodeExtensionResponse qrDetails = buyerOrderRepository.findQrCodeDetailsByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("해당 주문 ID에 대한 QR 코드를 찾을 수 없습니다: " + orderId));

        // 남은 연장 횟수 확인
        if (qrDetails.getCount() <= 0) {
            return "연장 실패: 사용 가능한 연장 횟수가 없습니다.";
        }

        LocalTime openingTime = qrDetails.getOpeningTime();
        LocalTime closingTime = qrDetails.getClosingTime();
        LocalDateTime currentValidUntil = qrDetails.getValidUntil();
        LocalDateTime nowKST = LocalDateTime.now(KST);

        // 24시간 영업점 확인
        // Case 1: 00:00 ~ 00:00 (opening == closing)
        // Case 2: 00:00 ~ 23:59:xx (opening == 00:00 and closing is 23:59)
        boolean is24Hours = openingTime.equals(closingTime) ||
                (openingTime.equals(LocalTime.MIDNIGHT) &&
                 closingTime.getHour() == 23 &&
                 closingTime.getMinute() == 59);

        // 24시간 영업이 아닐 경우에만 마감 시간 제약 체크
        if (!is24Hours) {
            LocalDateTime closingDateTime;

            // 마감 시간이 00:00:00인 경우, 날짜를 다음 날로 조정
            if (closingTime.equals(LocalTime.MIDNIGHT)) {
                // 현재 유효 시간의 날짜를 기준으로 다음 날 자정을 마감 시간으로 설정
                closingDateTime = currentValidUntil.toLocalDate().plusDays(1).atStartOfDay();
            } else {
                // 그 외의 경우, 현재 유효 시간의 날짜에 마감 시간 설정
                closingDateTime = currentValidUntil.toLocalDate().atTime(closingTime);
            }

            // 마감 1시간 전 시간 계산
            LocalDateTime extensionLimitTime = closingDateTime.minusHours(1);

            // 현재 시간이 이미 마감 1시간 이내인지 확인 -> 연장 불가
            if (nowKST.isAfter(extensionLimitTime)) {
                return "연장 실패: 마감 1시간 이내에는 연장이 불가능합니다.";
            }

            // 연장할 시간이 마감 1시간 전을 초과하는지 확인
            LocalDateTime proposedExtensionTime = currentValidUntil.plusHours(1);
            if (proposedExtensionTime.isAfter(extensionLimitTime)) {
                buyerOrderRepository.updateQrCode(orderId, extensionLimitTime, 0); // 마감 1시간 전으로 고정
                return "연장 성공: 마감 시간 제약으로 시간이 조정되었으며, 남은 횟수를 모두 사용합니다.";
            }
        }

        // 24시간이거나, 마감 제약에 걸리지 않는 일반적인 1시간 연장
        LocalDateTime proposedExtensionTime = currentValidUntil.plusHours(1);
        int newCount = qrDetails.getCount() - 1;
        buyerOrderRepository.updateQrCode(orderId, proposedExtensionTime, newCount);
        return "연장 성공: 유효기간이 1시간 연장되었습니다.";
    }

    /**
     *  구매중인 상품 구매 취소
     * */
    @Transactional
    public void cancelOrder(Long orderId) {
        // 1. 주문 정보를 조회 -> order id, quantity
        BuyerOrderCancelInfoResponse orderInfo = buyerOrderRepository.findOrderInfoById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 정보를 찾을 수 없습니다. orderId: " + orderId));

        // 2. products quantity 복구 (products.quantity += order_items.quantity)
        buyerOrderRepository.increaseProductQuantity(orderInfo.getProductId(), orderInfo.getQuantity());

        // 3. qr_codes 레코드를 제거
        buyerOrderRepository.deleteQrCodeByOrderId(orderId);

        // 4. order_items.status 판매취소로 변경
        buyerOrderRepository.updateorder_itemstatus(orderId, 0);

        // 5. 다른 진행 중인 주문이 있는지 확인
        int activeOrderCount = buyerOrderRepository.countActiveOrdersByProductId(orderInfo.getProductId());

        // 6. 다른 진행 중인 주문이 없다면, products.status 판매대기로 변경
        if (activeOrderCount == 0) {
            buyerOrderRepository.updateProductStatus(orderInfo.getProductId(), 0);
        }
    }

    /**
     *  구매 취소된 상품 출력
     * */
    public List<BuyerBuyCancelResponse> getBuyCancel(Long userId) {
        return buyerOrderRepository.getBuyCancel(userId);
    }

    /**
     *  구매 완료된 상품 출력
     * */
    public List<BuyerBuyCompletedResponse> getCompletedOrders(Long userId) {
        return buyerOrderRepository.getCompletedOrders(userId);
    }
}

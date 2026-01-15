package capstone.service.seller;

import capstone.dto.seller.response.SellerOrderResponse;
import capstone.dto.seller.response.SellerSaleHistoryResponse;
import java.util.List;
import java.util.Map;

/**
 * 판매/주문 관련 서비스의 모든 기능을 정의하는 유일한 인터페이스입니다.
 */
public interface SellerSaleService {

    /**
     * 상품별 '판매중'/'판매완료' 수량을 한번에 집계하여 반환합니다.
     * @param storeId 상점 ID
     * @return Key: productId, Value: [0]=판매중(예약) 수량, [1]=판매완료 수량
     */
    Map<Long, long[]> sumByProductForStore(Long storeId);

    /**
     * QR 코드를 이용해 주문을 '판매완료' 상태로 처리합니다.
     * @param orderId 주문 ID
     * @param uuid QR 코드의 UUID
     */
    void checkInWithQr(Long orderId, String uuid);

    /**
     * 주문을 '취소' 상태로 처리합니다.
     * @param orderId 주문 ID
     */
    void cancel(Long orderId);


    List<SellerOrderResponse> getReservationsForSeller(Long storeId);

    List<SellerOrderResponse> getCompletedForSeller(Long storeId);

    List<SellerSaleHistoryResponse> list(Long storeId, Object o);
}
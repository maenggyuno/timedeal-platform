package capstone.service.scheduler;

import capstone.repository.buyer.BuyerOrderRepository;
import capstone.service.buyer.BuyerOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class OrderCancellationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrderCancellationScheduler.class);

    private final BuyerOrderService buyerOrderService;
    private final BuyerOrderRepository buyerOrderRepository;

    public OrderCancellationScheduler(BuyerOrderService buyerOrderService, BuyerOrderRepository buyerOrderRepository) {
        this.buyerOrderService = buyerOrderService;
        this.buyerOrderRepository = buyerOrderRepository;
    }

    // 1분(60000ms)마다 실행되어 만료된 주문을 자동으로 취소
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredOrders() {
        logger.info("만료된 주문 자동 취소 스케줄러 실행...");

        // 1. 한국 시간에 맞게 현재 시간 생성
        LocalDateTime nowInKorea = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

        // 2. 현재 시간 기준으로 만료된 주문 ID 목록 조회
        List<Long> expiredOrderIds = buyerOrderRepository.findExpiredOrderIds(nowInKorea);

        if (expiredOrderIds.isEmpty()) {
            logger.info("취소할 만료된 주문이 없습니다.");
            return;
        }

        logger.info("Found {} expired orders to cancel: {}", expiredOrderIds.size(), expiredOrderIds);

        // 3. 각 주문에 대해 취소 처리
        for (Long orderId : expiredOrderIds) {
            try {
                buyerOrderService.cancelOrder(orderId);
                logger.info("주문 ID [{}] 취소 완료.", orderId);
            } catch (Exception e) {
                logger.error("주문 ID [{}] 자동 취소 중 오류 발생", orderId, e);
            }
        }
        logger.info("만료된 주문 자동 취소 작업 완료.");
    }
}

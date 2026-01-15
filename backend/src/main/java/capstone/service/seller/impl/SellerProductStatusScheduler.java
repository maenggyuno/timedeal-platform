package capstone.service.seller.impl;

// ✅ 250809 변경: 신규 파일
//8월 11일 시작 신규파일
import capstone.service.seller.SellerProductAutoStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerProductStatusScheduler {

    private final SellerProductAutoStatusService autoStatusService;

    // 250809 변경 :
    // 5분마다 전체 상품 상태 보정 (운영자 숨김은 유지)
    @Scheduled(fixedDelay = 5 * 60 * 1000L)
    public void syncAllProducts() {
        int changed = autoStatusService.runBulkSync();
        if (changed > 0) {
            log.info("[ProductStatusScheduler] auto-synced rows = {}", changed);
        }
    }
}

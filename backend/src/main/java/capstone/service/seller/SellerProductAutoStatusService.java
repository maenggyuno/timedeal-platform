package capstone.service.seller;

// 250809 변경: 신규 파일
// 8월 11일 시작 신규파일
public interface SellerProductAutoStatusService {
    void syncByProductId(Long productId);   // 재고/유통기한 기준 단건 동기화
    int runBulkSync();                      // 스케줄러가 호출하는 벌크 보정
}


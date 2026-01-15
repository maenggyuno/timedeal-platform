package capstone.domain;

public enum ProductStatus {
    PENDING,    // 판매대기 (DB : 0)
    AVAILABLE,  // 판매중 (DB : 1)
    SOLD_OUT    // 판매완료 (DB : 2)
}

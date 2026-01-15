package capstone.dto.seller.response;

//7월 28일 균오 생성

import capstone.domain.OrderStatus;

import java.time.LocalDateTime;
public record SellerSaleHistoryResponse(
        Long saleId,
        String productName,
        Long price,
        Integer quantity,
        String buyerName,
        LocalDateTime soldAt,
        Long totalPrice,
        OrderStatus status
){}

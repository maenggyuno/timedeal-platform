package capstone.dto.seller.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerReservationDto(
  @JsonProperty("order_id") Long orderId,
  @JsonProperty("product_id") Long productId,
  @JsonProperty("product_name") String productName,
  @JsonProperty("quantity") Integer quantity,
  @JsonProperty("user_name") String userName,
  @JsonProperty("valid_until") String validUntilIso, // ISO 문자열
  @JsonProperty("count") Integer count // qr_codes.count = 분
) {}

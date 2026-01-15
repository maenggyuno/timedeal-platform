package capstone.dto.seller.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerCompletedDto(
        @JsonProperty("order_id") Long orderId,
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_name") String productName,
        @JsonProperty("quantity") Integer quantity,
        @JsonProperty("sold_at") String soldAtIso,
        @JsonProperty("status") Integer status,
        @JsonProperty("user_name") String userName
) {}
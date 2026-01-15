package capstone.dto.seller.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SellerExpiringDto(
  @JsonProperty("product_id") Long productId,
  @JsonProperty("product_name") String productName,
  @JsonProperty("expiration_date") String expirationDateIso
) {}

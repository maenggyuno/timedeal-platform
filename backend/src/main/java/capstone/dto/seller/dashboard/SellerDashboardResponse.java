package capstone.dto.seller.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record SellerDashboardResponse(
  @JsonProperty("reservations")   List<SellerReservationDto> reservations,
  @JsonProperty("expiring")       List<SellerExpiringDto> expiring,
  @JsonProperty("completed")      List<SellerCompletedDto> completed
) {}

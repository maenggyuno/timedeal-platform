package capstone.controller.seller;

import capstone.dto.seller.dashboard.SellerCompletedDto;
import capstone.dto.seller.dashboard.SellerReservationDto;
import capstone.service.seller.SellerDashboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seller/stores/{martId}/dashboard")
public class SellerDashboardController {

  private final SellerDashboardService service;

  public SellerDashboardController(SellerDashboardService service) {
    this.service = service;
  }

  @GetMapping("/reservations")
  public Page<SellerReservationDto> reservations(@PathVariable Long martId, @RequestParam(defaultValue = "0") int page) {
    Pageable pageable = PageRequest.of(page, 10);
    return service.getReservations(martId, pageable);
  }

  @GetMapping("/purchases")
  public Page<SellerReservationDto> purchases(@PathVariable Long martId, @RequestParam(defaultValue = "0") int page) {
    Pageable pageable = PageRequest.of(page, 10);
    return service.getPurchases(martId, pageable);
  }

  @GetMapping("/completed")
  public Page<SellerCompletedDto> completed(@PathVariable Long martId, @RequestParam(defaultValue = "0") int page) {
    Pageable pageable = PageRequest.of(page, 10);
    return service.getCompleted(martId, pageable);
  }
}
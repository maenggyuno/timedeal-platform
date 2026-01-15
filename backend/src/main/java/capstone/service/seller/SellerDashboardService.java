package capstone.service.seller;

import capstone.dto.seller.dashboard.SellerCompletedDto;
import capstone.dto.seller.dashboard.SellerReservationDto;
import capstone.repository.seller.SellerDashboardRepository;
import capstone.repository.seller.proj.CompletedRow;
import capstone.repository.seller.proj.ReservationRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class SellerDashboardService {

  private final SellerDashboardRepository repo;
  private final DateTimeFormatter iso = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

  public SellerDashboardService(SellerDashboardRepository repo) {
    this.repo = repo;
  }

  private String formatIso(java.util.Date date) {
    if (date == null) return null;
    return date.toInstant().atZone(ZoneId.systemDefault()).format(iso);
  }

  public Page<SellerReservationDto> getReservations(Long martId, Pageable pageable) {
    Page<ReservationRow> rows = repo.findReservations(martId, pageable);
    return rows.map(r -> new SellerReservationDto(
            r.getOrderId(), r.getProductId(), r.getProductName(),
            r.getQuantity(), r.getUserName(), formatIso(r.getValidUntil()), r.getQrCount()
    ));
  }

  public Page<SellerReservationDto> getPurchases(Long martId, Pageable pageable) {
    Page<ReservationRow> rows = repo.findPurchases(martId, pageable);
    return rows.map(r -> new SellerReservationDto(
            r.getOrderId(), r.getProductId(), r.getProductName(),
            r.getQuantity(), r.getUserName(), formatIso(r.getValidUntil()), r.getQrCount()
    ));
  }

  public Page<SellerCompletedDto> getCompleted(Long martId, Pageable pageable) {
    Page<CompletedRow> rows = repo.findCompleted(martId, pageable);
    return rows.map(e -> new SellerCompletedDto(
            e.getOrderId(), e.getProductId(), e.getProductName(),
            e.getQuantity(), formatIso(e.getSoldAt()), e.getStatus(), e.getUserName()
    ));
  }
}
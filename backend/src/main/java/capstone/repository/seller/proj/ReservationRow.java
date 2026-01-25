// src/main/java/capstone/repository/seller/proj/ReservationRow.java
package capstone.repository.seller.proj;

import java.sql.Timestamp;

public interface ReservationRow {
  Long getOrderId();
  Long getProductId();
  String getProductName();
  Integer getQuantity();
  String getUserName();
  Timestamp getValidUntil();
  Integer getQrCount(); // qr_codes.count (분)
}

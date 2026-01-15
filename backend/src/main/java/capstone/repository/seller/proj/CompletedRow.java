// src/main/java/capstone/repository/seller/proj/CompletedRow.java
package capstone.repository.seller.proj;

import java.sql.Timestamp;

public interface CompletedRow {
  Long getOrderId();
  Long getProductId();
  String getProductName();
  Integer getQuantity();
  Timestamp getSoldAt();
  Integer getStatus(); // 4
  String getUserName();
}
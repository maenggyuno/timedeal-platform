// src/main/java/capstone/repository/seller/proj/ExpiringRow.java
package capstone.repository.seller.proj;

import java.sql.Timestamp;

public interface ExpiringRow {
  Long getProductId();
  String getProductName();
  Timestamp getExpirationDate();
}
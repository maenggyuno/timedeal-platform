package capstone.dto.buyer.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class BuyerStoreMapResponse {

    private Long storeId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phoneNumber;
    private String businessHours;   // opening_time + closing_time

    public BuyerStoreMapResponse(Long storeId, String name, String address, BigDecimal latitude, BigDecimal longitude, String phoneNumber, LocalTime openingHours, LocalTime closingHours) {
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.businessHours = setBusinessHours(openingHours, closingHours);
    }

    public String setBusinessHours(LocalTime open, LocalTime close) {
        if (open == null || close == null) {
            this.businessHours = "정보 없음";
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return open.format(formatter) + " - " + close.format(formatter);
    }
}

package capstone.dto.buyer.response;

import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
public class BuyerStoreLocationResponse {

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String name;
    private String address;
    private String phoneNumber;
    private String businessHours;    // opening_time + closing_time

    public BuyerStoreLocationResponse(BigDecimal latitude, BigDecimal longitude, String name, String address, String phoneNumber, Time openingTime, Time closingTime) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.businessHours = setBusinessHours(openingTime, closingTime);
    }

    public String setBusinessHours(Time openingTime, Time closingTime) {

        LocalTime open = null;
        if (openingTime != null) {
            open = openingTime.toLocalTime();
        }

        LocalTime close = null;
        if (closingTime != null) {
            close = closingTime.toLocalTime();
        }

        if (open == null || close == null) {
            this.businessHours = "정보 없음";
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return open.format(formatter) + " - " + close.format(formatter);
    }
}

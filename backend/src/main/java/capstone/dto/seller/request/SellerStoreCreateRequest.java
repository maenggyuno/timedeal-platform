package capstone.dto.seller.request;

import capstone.domain.Store;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerStoreCreateRequest {

    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("business_number")
    private String businessNumber;

    @JsonProperty("payment_method")
    private Integer paymentMethod;

    @JsonProperty("opening_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openingTime;

    @JsonProperty("closing_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closingTime;

    // Store 엔티티로 변환하는 메서드
    public Store toEntity(Long ownerId) {
        Store store = new Store();
        store.setName(this.name);
        store.setAddress(this.address);
        store.setLatitude(this.latitude);
        store.setLongitude(this.longitude);
        store.setPhoneNumber(this.phoneNumber);
        store.setBusinessNumber(this.businessNumber);
        store.setOpeningTime(this.openingTime);
        store.setClosingTime(this.closingTime);
        store.setPaymentMethod(this.paymentMethod);
        store.setOwnerId(ownerId);
        return store;
    }
}

package capstone.dto.seller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

public class SellerStoreUpdateRequest {

    @Getter @Setter @NoArgsConstructor
    public static class Name {
        private String name;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Address {
        private String address;
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PhoneNumber {
        private String phoneNumber;
    }

    @Getter @Setter @NoArgsConstructor
    public static class OperatingHours {
        private LocalTime openingTime;
        private LocalTime closingTime;
    }

    @Getter @Setter @NoArgsConstructor
    public static class PaymentMethod {
        private Integer paymentMethod;
    }
}

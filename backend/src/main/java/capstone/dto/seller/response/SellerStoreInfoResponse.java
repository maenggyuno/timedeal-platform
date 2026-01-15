package capstone.dto.seller.response;

import capstone.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@AllArgsConstructor
public class SellerStoreInfoResponse {

    private String name;
    private String address;

    private String phoneNumber;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Integer paymentMethod;
    private String businessNumber;

    public static SellerStoreInfoResponse from(Store store) {
        return new SellerStoreInfoResponse(
                store.getName(),
                store.getAddress(),
                store.getPhoneNumber(),
                store.getOpeningTime(),
                store.getClosingTime(),
                store.getPaymentMethod(),
                store.getBusinessNumber()
        );
    }
}

package capstone.dto.seller;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerStoreDto {

    private Long id;
    private String storeName;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String businessNumber;
    private Long ownerId;
    private Integer paymentMethod;

    @JsonFormat(pattern = "HH:mm[:ss]")   // "14:21" 또는 "14:21:00" 모두 OK
    private LocalTime openTime;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime closeTime;
}
package capstone.dto.seller;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SellerStoreInfoDto {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String openTime;
    private String closeTime;
    private Double latitude;
    private Double longitude;
}

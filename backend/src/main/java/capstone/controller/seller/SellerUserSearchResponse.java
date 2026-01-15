package capstone.controller.seller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SellerUserSearchResponse {
    private Long userId;
    private String name;
    private String email;
    private String status; // "OK", "NOT_FOUND", "ALREADY_EMPLOYEE"
}
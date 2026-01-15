package capstone.dto.seller.response;

import capstone.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SellerEmployeeListResponse {

    private Long userId;
    private String name;
    private String email;
    private String position; // "관리자" 또는 "직원"

    public static SellerEmployeeListResponse of(User user, int authority) {
        String position = (authority == 1) ? "관리자" : "직원";
        return new SellerEmployeeListResponse(user.getUserId(), user.getName(), user.getEmail(), position);
    }
}

package capstone.dto.seller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class SellerEmployeeRequest {

    @Getter @NoArgsConstructor
    public static class DelegateManager {
        private Long newManagerUserId;
    }

    @Getter @NoArgsConstructor
    public static class DeleteEmployees {
        private List<Long> userIds;
    }

    @Getter @NoArgsConstructor
    public static class AddEmployees {
        private List<Long> userIds;
    }
}

package capstone.controller.seller;

import capstone.dto.seller.request.SellerEmployeeRequest;
import capstone.dto.seller.response.SellerEmployeeListResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.seller.SellerEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seller/store/{storeId}/employees")
@RequiredArgsConstructor
public class SellerEmployeeController {
    private final SellerEmployeeService sellerEmployeeService;

    @GetMapping
    public ResponseEntity<List<SellerEmployeeListResponse>> getEmployees(@PathVariable Long storeId) {
        List<SellerEmployeeListResponse> employees = sellerEmployeeService.findEmployeesByStoreId(storeId);
        return ResponseEntity.ok(employees);
    }

    /**
     * 총괄 관리자 권한 위임
     */
    @PostMapping("/delegate")
    public ResponseEntity<Void> delegateManager(
            @PathVariable Long storeId,
            @RequestBody SellerEmployeeRequest.DelegateManager request,
            @AuthenticationPrincipal UserPrincipal currentUser) { // 현재 로그인한 사용자 정보

        sellerEmployeeService.delegateManager(storeId, currentUser.getUserId(), request.getNewManagerUserId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteEmployees(@PathVariable Long storeId, @RequestBody SellerEmployeeRequest.DeleteEmployees request) {
        sellerEmployeeService.deleteEmployees(storeId, request.getUserIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search-user")
    public ResponseEntity<SellerUserSearchResponse> searchUserByEmail(@PathVariable Long storeId, @RequestParam String email) {
        SellerUserSearchResponse response = sellerEmployeeService.searchUserByEmail(storeId, email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addEmployees(@PathVariable Long storeId, @RequestBody SellerEmployeeRequest.AddEmployees request) {
        sellerEmployeeService.addEmployees(storeId, request.getUserIds());
        return ResponseEntity.ok().build();
    }
}

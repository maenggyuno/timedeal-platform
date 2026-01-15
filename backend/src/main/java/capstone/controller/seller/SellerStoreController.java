package capstone.controller.seller;

import capstone.domain.Store;
import capstone.dto.seller.SellerMyMartInfoResponse;
import capstone.dto.seller.request.SellerStoreCreateRequest;
import capstone.dto.seller.request.SellerStoreUpdateRequest;
import capstone.dto.seller.response.SellerDashboardResponse;
import capstone.dto.seller.response.SellerStoreInfoResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.seller.SellerStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seller/store")
public class SellerStoreController {

    private final SellerStoreService sellerStoreService;

    public SellerStoreController(SellerStoreService sellerStoreService) {
        this.sellerStoreService = sellerStoreService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<SellerDashboardResponse>> getAllStores(@AuthenticationPrincipal UserPrincipal user) {
        List<SellerDashboardResponse> stores = sellerStoreService.findAll(user.getUserId());
        return ResponseEntity.ok(stores);
    }

    @PostMapping("/create")
    public ResponseEntity<Store> create(@AuthenticationPrincipal UserPrincipal user,
                                        @RequestBody SellerStoreCreateRequest request) {

        Store createdStore = sellerStoreService.create(user.getUserId(), request);

        return ResponseEntity.created(URI.create("/api/seller/store/" + createdStore.getStoreId()))
                .body(createdStore);
    }

    @GetMapping("/{storeId}/mymart")
    public ResponseEntity<SellerMyMartInfoResponse> getMyMartInfo(@PathVariable Long storeId) {
        SellerMyMartInfoResponse myMartInfo = sellerStoreService.getMyMartInfo(storeId);
        return ResponseEntity.ok(myMartInfo);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<SellerStoreInfoResponse> getStoreInfo(@PathVariable Long storeId) {
        SellerStoreInfoResponse storeInfo = sellerStoreService.getStoreInfo(storeId);
        return ResponseEntity.ok(storeInfo);
    }

    @PatchMapping("/{storeId}/name")
    public ResponseEntity<Void> updateStoreName(@PathVariable Long storeId, @RequestBody SellerStoreUpdateRequest.Name request) {
        sellerStoreService.updateName(storeId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{storeId}/address")
    public ResponseEntity<Void> updateStoreAddress(@PathVariable Long storeId, @RequestBody SellerStoreUpdateRequest.Address request) {
        sellerStoreService.updateAddress(storeId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{storeId}/phone-number")
    public ResponseEntity<Void> updateStorePhoneNumber(@PathVariable Long storeId, @RequestBody SellerStoreUpdateRequest.PhoneNumber request) {
        sellerStoreService.updatePhoneNumber(storeId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{storeId}/hours")
    public ResponseEntity<Void> updateStoreHours(@PathVariable Long storeId, @RequestBody SellerStoreUpdateRequest.OperatingHours request) {
        sellerStoreService.updateHours(storeId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{storeId}/payment-method")
    public ResponseEntity<Void> updateStorePaymentMethod(@PathVariable Long storeId, @RequestBody SellerStoreUpdateRequest.PaymentMethod request) {
        sellerStoreService.updatePaymentMethod(storeId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long storeId) {
        sellerStoreService.deleteStore(storeId);
        return ResponseEntity.ok().build();
    }
}
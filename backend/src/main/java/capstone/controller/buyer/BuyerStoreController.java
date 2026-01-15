package capstone.controller.buyer;

import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buyer/stores")
public class BuyerStoreController {

    private final BuyerStoreService storeService;

    public BuyerStoreController(BuyerStoreService storeService) {
        this.storeService = storeService;
    }

    /**
     *  상점 팔로우 기능
     * */
    @PostMapping("/followStore/{store_id}")
    public ResponseEntity<Void> followStore(@AuthenticationPrincipal UserPrincipal user,
                                            @PathVariable("store_id") Long storeId) {
        storeService.followStore(user.getUserId(), storeId);
        return ResponseEntity.ok().build();
    }

    /**
     *  상점 언팔로우 기능
     * */
    @PostMapping("/unfollowStore/{store_id}")
    public ResponseEntity<Void> unfollowStore(@AuthenticationPrincipal UserPrincipal user,
                                              @PathVariable("store_id") Long storeId) {
        storeService.unfollowStore(user.getUserId(), storeId);
        return ResponseEntity.ok().build();
    }

}

package capstone.controller.buyer;

import capstone.dto.buyer.request.BuyerAddCartRequest;
import capstone.dto.buyer.request.BuyerDeleteCartItemsRequest;
import capstone.dto.buyer.response.BuyerCartItemResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerCartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/carts")
public class BuyerCartController {

    public BuyerCartController(BuyerCartService cartService) {
        this.cartService = cartService;
    }

    private final BuyerCartService cartService;

    /**
     *  장바구니 상품 추가
     * */
    @PostMapping("/addCart")
    public ResponseEntity<Void> addToCart(@AuthenticationPrincipal UserPrincipal user,
                                          @RequestBody BuyerAddCartRequest request) {
        cartService.addToCart(user.getUserId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    /**
     *  장바구니 상품 목록 확인
     * */
    @GetMapping("/all")
    public List<BuyerCartItemResponse> getCarts(@AuthenticationPrincipal UserPrincipal user) {
        return cartService.getCartItemsByUserId(user.getUserId());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCartItems(@AuthenticationPrincipal UserPrincipal user,
                                                @RequestBody BuyerDeleteCartItemsRequest request) {
        cartService.deleteCartItems(user.getUserId(), request.getProductIds());
        return ResponseEntity.ok().build();
    }
}

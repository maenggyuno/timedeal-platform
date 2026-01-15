package capstone.controller.buyer;

import capstone.dto.buyer.response.BuyerProductDetailResponse;
import capstone.dto.buyer.response.BuyerProductSearchResponse;
import capstone.security.dto.UserPrincipal;
import capstone.service.buyer.BuyerProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/products")
@CrossOrigin(origins = "http://localhost:3000")
public class BuyerProductController {

    private final BuyerProductService productService;

    public BuyerProductController(BuyerProductService productService) {
        this.productService = productService;
    }

    /**
     *  메인화면
     *  - 상품목록출력
     * */
    @GetMapping("/nearby")
    public ResponseEntity<List<BuyerProductSearchResponse>> getNearbyProducts(@RequestParam("lat") double latitude,
                                                                              @RequestParam("lng") double longitude,
                                                                              @RequestParam("distance") double distance,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "30") int size) {
        List<BuyerProductSearchResponse> products = productService.getProductsNearby(latitude, longitude, distance, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     *  선택한 상품정보확인
     * */
    @GetMapping("/{productId}")
    public ResponseEntity<BuyerProductDetailResponse> getProductById(@AuthenticationPrincipal UserPrincipal user,
                                                     @PathVariable Long productId) {
        // 로그인을 하지 않았을경우, userId는 null
        Long userId = null;
        if (user != null) {
            userId = user.getUserId();
        }

        BuyerProductDetailResponse product = productService.getProductById(userId, productId);
        return ResponseEntity.ok().body(product);
    }

    /**
     *  카테고리 기능
     * */
    @GetMapping("/search/category")
    public ResponseEntity<List<BuyerProductSearchResponse>> getNearbyProductsByCategory(@RequestParam("lat") double latitude,
                                                                                        @RequestParam("lng") double longitude,
                                                                                        @RequestParam("distance") double distance,
                                                                                        @RequestParam("category") String category,
                                                                                        @RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "30") int size) {
        List<BuyerProductSearchResponse> products = productService.getNearbyProductsByCategory(latitude, longitude, distance, category, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     *  검색기능
     * */
    @GetMapping("/search")
    public ResponseEntity<List<BuyerProductSearchResponse>> getNearbyProductsBySearchString(@RequestParam("lat") double latitude,
                                                                                            @RequestParam("lng") double longitude,
                                                                                            @RequestParam("distance") double distance,
                                                                                            @RequestParam("search") String searchString,
                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "30") int size) {
        List<BuyerProductSearchResponse> products = productService.getNearbyProductsBySearchString(latitude, longitude, distance, searchString, page, size);
        return ResponseEntity.ok(products);
    }

    /**
     *  지도 모달에서 상점 상품 띄우는 거
     * */
    @GetMapping("/{storeId}/products")
    public ResponseEntity<List<BuyerProductSearchResponse>> getProductsByStore(@PathVariable Long storeId,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "12") int size) {
        List<BuyerProductSearchResponse> products = productService.getProductsByStore(storeId, page, size);
        return ResponseEntity.ok(products);
    }
}

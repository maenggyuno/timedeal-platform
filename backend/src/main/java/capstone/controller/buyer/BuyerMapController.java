package capstone.controller.buyer;

import capstone.dto.buyer.response.BuyerStoreLocationResponse;
import capstone.dto.buyer.response.BuyerStoreMapResponse;
import capstone.service.buyer.BuyerMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buyer/maps")
public class BuyerMapController {

    private final BuyerMapService mapService;

    public BuyerMapController(BuyerMapService mapService) {
        this.mapService = mapService;
    }

    /**
     *  모든 상점 검색
     *  - 현재는 사용하지는 않음
     * */
    @GetMapping("/findAllStores")
    public ResponseEntity<List<BuyerStoreMapResponse>> getAllStores() {
        List<BuyerStoreMapResponse> maps = mapService.getAllStores();
        return ResponseEntity.ok(maps);
    }

    /**
     *  근처 상점 검색
     * */
    @GetMapping("/nearbyStores")
    public ResponseEntity<List<BuyerStoreMapResponse>> getNearbyStores(@RequestParam("lat") double latitude,
                                                                       @RequestParam("lng") double longitude) {
        List<BuyerStoreMapResponse> maps = mapService.getNearbyStores(latitude, longitude);
        return ResponseEntity.ok(maps);
    }

    /**
     *  상점 정보
     * */
    @GetMapping("/{storeId}")
    public ResponseEntity<BuyerStoreLocationResponse> getStoreLocation(@PathVariable("storeId") Long storeId) {
        BuyerStoreLocationResponse response = mapService.getStoreLocation(storeId);
        return ResponseEntity.ok(response);
    }
}

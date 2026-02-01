package capstone.controller.seller.sgis;

import capstone.service.seller.sgis.SgisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/sgis")
@RequiredArgsConstructor
public class SgisController {

    private final SgisService sgisService;

    /**
     * 주소 -> 좌표 변환 (Geocoding)
     * 프론트 사용 예: /api/sgis/geocode?address=서울시 강남구...
     */
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Object>> getGeocode(@RequestParam("address") String address) {
        Map<String, Object> result = sgisService.getGeocode(address);
        return ResponseEntity.ok(result);
    }
}
